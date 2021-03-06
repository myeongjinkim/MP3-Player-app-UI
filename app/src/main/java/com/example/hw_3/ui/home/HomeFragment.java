package com.example.hw_3.ui.home;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.hw_3.R;
import com.example.hw_3.databinding.FragmentHomeBinding;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import java.io.File;
import java.io.IOException;

public class HomeFragment extends Fragment {

    //ui 및 구조
    private HomeViewModel homeViewModel;
    private FragmentTransaction ft;
    public FragmentHomeBinding binding;
    private HomeLyricsFragment homeLyricsFragment;
    private HomeJacketFragment homeJacketFragment;
    private boolean start = false;
    private boolean check =true;
    private TextView titleText;
    private TextView artistText;

    //mp3
    private MediaPlayer mediaPlayer;
    private SeekBar seekbar;
    private int nowSeek;
    private int maxSeek;
    private TextView maxSeekText;
    private TextView nowSeekText;
    private File fs;
    private StringBuilder path;
    private String musicPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 화면 전환 프래그먼트 선언 및 초기 화면 설정
        homeLyricsFragment = new HomeLyricsFragment();
        homeJacketFragment = new HomeJacketFragment();
        path=new StringBuilder();


    }
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        titleText = (TextView) rootView.findViewById(R.id.music_title);
        artistText = (TextView) rootView.findViewById(R.id.music_artist);
        nowSeekText = (TextView) rootView.findViewById(R.id.nowSeekTextView);
        maxSeekText = (TextView) rootView.findViewById(R.id.maxSeekTextView);


        path.append("/data/data/com.example.hw_3/music/");
        fs = new File(path.toString());

        music();
        mediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(musicPath));
        seekbar = (SeekBar)rootView.findViewById(R.id.seekBar);
        maxSeek= mediaPlayer.getDuration();
        seekbar.setMax(maxSeek);

        maxSeekText.setText(changeTime(maxSeek));

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                if(fromUser) {
                    mediaPlayer.seekTo(progress);
                    nowSeek = progress;
                    nowSeekText.setText(changeTime(nowSeek));
                }
            }
        });

        if( homeViewModel.getCheck()){
            replaceToJacket();
        }else{
            replaceToLyrics();
        }

        binding = DataBindingUtil.bind(rootView);
        binding.setFragment(this);
        return rootView;
    }

    public void replaceToJacket(){
        ft = getChildFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.replace, homeJacketFragment);
        ft.commit();
    }
    public void replaceToLyrics(){
        ft = getChildFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.replace, homeLyricsFragment);
        ft.commit();
    }
    public void pressPlay(View v){
        if(mediaPlayer.isPlaying()){
            ((ImageButton)v).setImageResource(R.drawable.ic_action_play);
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            ((ImageButton)v).setImageResource(R.drawable.ic_action_stop);
            nowSeek = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(nowSeek); // 일시정지 시점으로 이동
            mediaPlayer.start();
            Thread();
        }
    }
    public void Thread(){
        Runnable task = new Runnable(){
            public void run(){
                while(mediaPlayer.isPlaying()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Message msg =handler.obtainMessage();
                    handler.sendMessage(msg);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }
    Handler handler = new Handler(){

        public void handleMessage(Message msg){
            nowSeek = mediaPlayer.getCurrentPosition();
            seekbar.setProgress(nowSeek);
            nowSeekText.setText(changeTime(nowSeek));
        }
    };
    private String changeTime(int intTime){
        String min;
        String sec;
        intTime = intTime/1000;
        if((intTime/60)<10){
            min = "0"+Integer.toString(intTime/60);
        }else{
            min = Integer.toString(intTime/60);
        }
        if((intTime%60)<10){
            sec= "0"+Integer.toString(intTime%60);
        }else{
            sec= Integer.toString(intTime%60);
        }
        return min+":"+sec;
    }
    public void music(){

        if(fs.isDirectory()){
            System.out.println("들어감");
            String decoding = "ISO-8859-1";
            String encoding = "EUC-KR";

            File list[] = fs.listFiles();
            for(File f : list){
                try{
                    MP3File mp3 = (MP3File) AudioFileIO.read(f);
                    musicPath = f.getPath();
                    AbstractID3v2Tag tag2 = mp3.getID3v2Tag();
                    Tag tag = mp3.getTag();
                    homeViewModel.LyricsSetting(tag.getFirst(FieldKey.LYRICS));
                    titleText.setText(tag.getFirst(FieldKey.TITLE));
                    artistText.setText(tag.getFirst(FieldKey.ARTIST));

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        else {
            System.out.println("경로 틀림");
        }
    }

}