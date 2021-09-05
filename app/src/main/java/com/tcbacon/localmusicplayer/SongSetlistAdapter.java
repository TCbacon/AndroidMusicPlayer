package com.tcbacon.localmusicplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SongSetlistAdapter extends RecyclerView.Adapter<SongSetlistAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private ArrayList<Song> songListFull;
    private ArrayList<Song> songList;
    private SongDatabase db;
    private  MediaMetadataRetriever mmr;
    private AlertDialog alert;

    //Constructor
    public SongSetlistAdapter(SongDatabase db, Context mContext){
        this.mContext = mContext;
        songListFull = new ArrayList<>();
        songList = new ArrayList<>();
        this.db = db;
        this.mmr = new MediaMetadataRetriever();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_music_setlist, parent, false);
        return new ViewHolder(view);
    }

    /**
     * holder refers to  manually created  ViewHolder inner class
     * position refers to position inside array list
     * @param holder holds the cardview
     * @param position item that is selected
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //songlist changes when user searches

        //to reference a song for setting info or deleting
        Song song = songList.get(position);

        //set the info of songs in list
        setTxtViewInfo(holder, song);

        initImgViewSong(holder, songListFull.indexOf(song));
        songInfoIntentPassHandler(holder, songListFull.indexOf(song));
        btnCustomHandlerAlertDialog(holder, song);
        moreSongInfoHandler(holder);
        favoriteSongsHandler(song, holder);
    }

    /**
     * Set the song info in text view in bind view holder
     * @param holder viewHolder
     * @param song song obj
     */
    private void setTxtViewInfo(@NonNull ViewHolder holder, Song song) {

        //set title to specified sort title
        switch (MusicSetlistActivity.sortTrackerNumber){
                case 1:
                    displaySortByTitle(holder, song.getArtist());
                    break;
                case 2:
                    displaySortByTitle(holder, song.getAlbum());
                    break;
                case 3:
                    displaySortByTitle(holder, String.valueOf(song.getYear()));
                    break;
                default:
                    displaySortByTitle(holder, song.getName());
                    break;
            }

            holder.txtViewSongTitleMoreInfo.setText(song.getName());
        holder.txtViewSongAlbum.setText(song.getAlbum());
        holder.txtViewSongArtist.setText(song.getArtist());

        if (song.getYear() == 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            holder.txtViewSongYear.setText("0");
        } else {
            holder.txtViewSongYear.setText(String.valueOf(song.getYear()));
        }

    }

    private void displaySortByTitle(@NonNull ViewHolder holder, String sortTitle) {
        holder.txtViewSongTitle.setText(sortTitle);
    }

    /**
     * handles checking or unchecking favorite songs
     * @param song current song selected
     * @param holder viewholder
     */
    private void favoriteSongsHandler(Song song, @NonNull ViewHolder holder) {

        //PREVENTS BUG IN FAVORITES CHECKBOX
        holder.chkBoxFavoriteSong.setOnCheckedChangeListener(null);

        holder.chkBoxFavoriteSong.setChecked(song.getIsFavorite() == 1);


        holder.chkBoxFavoriteSong.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            song.setIsFavorite(1);
                            db.songDAO().update(song);

                        }
                    });

                } else {

                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            song.setIsFavorite(0);
                            db.songDAO().update(song);
                        }
                    });
                }
            }

        });
    }

    private void moreSongInfoHandler(@NonNull ViewHolder holder) {
        holder.imgViewDownArrow.setOnClickListener(new View.OnClickListener() {

            boolean isMoreInfoShown = false;

            @Override
            public void onClick(View view) {
                if(!isMoreInfoShown){
                    holder.relLayoutMoreInfo.setVisibility(View.VISIBLE);
                    holder.imgViewDownArrow.setImageResource(R.drawable.ic_up_arrow);
                    isMoreInfoShown = true;
                }

                else{
                    holder.relLayoutMoreInfo.setVisibility(View.GONE);
                    holder.imgViewDownArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                    isMoreInfoShown = false;
                }

            }
        });
    }


    /**
     * Config song info from recycler view and song database
     * @param holder the view holder
     * @param song song object to delete
     */
    private void btnCustomHandlerAlertDialog(@NonNull ViewHolder holder, Song song){
        holder.btnSetlistEditSongInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                final ViewGroup nullParent = null;
                View promptView = layoutInflater.inflate(R.layout.custom_dialog, nullParent);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.SetlistAlertDialogStyle);
                alert = builder.create();
                builder.setTitle("Select an option");

                //HANDLES THE CUSTOM BUTTOMS
                btnBackCustomHandler(promptView, alert);
                btnDeleteCustomHandler(promptView, alert, song);

                Button btnEditSongInfo = promptView.findViewById(R.id.btnEditSongInfo);
                btnEditSongInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, EditSongInfoActivity.class);
                        intent.putExtra(EditSongInfoActivity.SONG_KEY, song);
                        mContext.startActivity(intent);
                    }
                });

                alert.setView(promptView);
                alert.show();
            }
        });
    }


    private void btnBackCustomHandler(View promptView, AlertDialog alert) {
        Button btnBackCustom = promptView.findViewById(R.id.btnBackCustom);
        btnBackCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.cancel();
            }
        });

    }

    private void btnDeleteCustomHandler(View promptView, AlertDialog alert, Song song) {
        Button btnDeleteCustom = promptView.findViewById(R.id.btnDeleteCustom);
        btnDeleteCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(mContext)
                        .setTitle("Delete Selected Song?")
                        .setMessage(song.getName())
                        .setNegativeButton("No",null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //revoke permission to custom image when deleting a song
                                try {
                                    mContext.getContentResolver().releasePersistableUriPermission(Uri.parse(song.getCustomImageUri()),  Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }

                                catch (SecurityException e){
                                    //pass
                                }

                                Executors.newSingleThreadExecutor().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        db.songDAO().delete(song);
                                    }
                                });

                                songListFull.remove(song);
                                songList.remove(song);
                                notifyDataSetChanged();

                            }
                        }).show();

                alert.dismiss();

            }
        });
    }


    /**
     * Pass the song info to songActivity.class
     * @param holder view holder
     * @param position position in recycler view
     */
    private void songInfoIntentPassHandler(@NonNull ViewHolder holder, int position) {
        holder.btnSetlistToSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handles passing large data to song activity
                SongActivity.songs = songListFull;

                Intent intent = new Intent(mContext, SongActivity.class);
                //get index of song in arraylist rather than using position in recycling view when filtering
                intent.putExtra(SongActivity.SETLIST_POS, songListFull.indexOf(songListFull.get(position)));

                mContext.startActivity(intent);
            }
        });
    }

    /**
     * Initialize the images in the song setlist recycler view
     * @param holder view holder
     * @param position in array list
     */
    private void initImgViewSong(@NonNull ViewHolder holder, int position) {

                try {
                    if(!songListFull.get(position).getCustomImageUri().equals("")){

                        Uri uriTemp = Uri.parse(songListFull.get(position).getCustomImageUri());

                        //loading images using picasso to prevent lagging when scrolling through recyling view
                        Picasso.get().load(uriTemp).fit().centerCrop().rotate(songListFull.get(position).getRotation()).placeholder(R.drawable.lmp_img_placeholder).into(holder.songSetlistImgView, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                holder.songSetlistImgView.setImageResource(R.drawable.lmp_missing_image);
                            }
                        });
                    }

                    else {
                        Uri uriTemp = Uri.parse(songListFull.get(position).getDefaultSongUri());

                        mmr.setDataSource(mContext, uriTemp);
                        byte[] data = mmr.getEmbeddedPicture();

                        // convert the byte array to a bitmap
                        if (data != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                            holder.songSetlistImgView.setImageBitmap(bitmap);
                            holder.songSetlistImgView.setAdjustViewBounds(true);

                        }

                        else {
                            holder.songSetlistImgView.setImageResource(R.drawable.lmp_missing_image);
                        }
                        holder.songSetlistImgView.setAdjustViewBounds(true);

                    }
                }

                catch (Exception e) {
                    holder.songSetlistImgView.setImageResource(R.drawable.lmp_missing_image);
                }
    }

    //displays the number of items in array list in cardView
    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void setSongListFull(ArrayList<Song> songListFull) {
        this.songListFull = songListFull;
        songList = new ArrayList<>(songListFull);
        notifyDataSetChanged();

    }

    @Override
    public Filter getFilter() {
        return songFilterHandler;
    }

    private Filter songFilterHandler = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Song> filteredSongs = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length() == 0){
                filteredSongs.addAll(songListFull);
            }

            else{
                String filterPattern =charSequence.toString().toLowerCase().trim();
                for (Song item : songListFull) {
                    filterSongSort(filteredSongs, filterPattern, item);
                }
            }

            results.values = filteredSongs;
            results.count = filteredSongs.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            songList.clear();
            songList.addAll((ArrayList) filterResults.values);
            notifyDataSetChanged();
        }
    };

    /**
     * Filter songs based on which sort the user selected. Sort by: name, artist, album, year
     * @param filteredSongs filtered song list
     * @param filterPattern pattern is search bar
     * @param song song object
     */
    private void filterSongSort(List<Song> filteredSongs, String filterPattern, Song song){
        if(MusicSetlistActivity.sortTrackerNumber == 1){
            if (song.getArtist().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                filteredSongs.add(song);
            }
        }

        else if(MusicSetlistActivity.sortTrackerNumber == 2){
            if (song.getAlbum().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                filteredSongs.add(song);
            }
        }

        else if(MusicSetlistActivity.sortTrackerNumber == 3){
            if (String.valueOf(song.getYear()).contains(filterPattern)) {
                filteredSongs.add(song);
            }
        }

        else{
            if (song.getName().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                filteredSongs.add(song);
            }
        }
    }

    /**
     * To prevent leaks in alert dialog when user rotates device
     * @return onPause()
     */
    public AdapterLifeCycleState  registerActivityState() {
      return new AdapterLifeCycleState() {

          @Override
          public void onPaused() {
              if(alert != null){
                  alert.dismiss();
              }
          }
      };
    }


    /**
     * Card View holder
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        private Button btnSetlistToSong;
        private Button btnSetlistEditSongInfo;
        private ImageView imgViewDownArrow;
        private RelativeLayout relLayoutMoreInfo;
        private ImageView songSetlistImgView;

        private TextView txtViewSongTitle;
        private TextView txtViewSongTitleMoreInfo;
        private TextView txtViewSongAlbum;
        private TextView txtViewSongArtist;
        private TextView txtViewSongYear;

        private CheckBox chkBoxFavoriteSong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtViewSongTitle = itemView.findViewById(R.id.txtViewSongTitleSetlist);
            txtViewSongTitle.setSelected(true);
            songSetlistImgView = itemView.findViewById(R.id.imgViewSetlistSong);

            btnSetlistToSong = itemView.findViewById(R.id.btnSetlistToSong);
            btnSetlistEditSongInfo = itemView.findViewById(R.id.btnSetlistEditSongInfo);

            imgViewDownArrow = itemView.findViewById(R.id.imgViewDownArrow);
            relLayoutMoreInfo = itemView.findViewById(R.id.relLayoutMoreInfo);

            txtViewSongTitleMoreInfo = itemView.findViewById(R.id.txtViewSongTitleMoreInfo);
            txtViewSongTitleMoreInfo.setSelected(true);
            txtViewSongAlbum = itemView.findViewById(R.id.txtViewSongAlbum);
            txtViewSongAlbum.setSelected(true);
            txtViewSongArtist = itemView.findViewById(R.id.txtViewSongArtist);
            txtViewSongArtist.setSelected(true);
            txtViewSongYear = itemView.findViewById(R.id.txtViewSongYear);

            chkBoxFavoriteSong = itemView.findViewById(R.id.chkBoxFavoriteSong);
        }
    }


}
