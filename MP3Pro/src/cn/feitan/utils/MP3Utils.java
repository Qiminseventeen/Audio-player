package cn.feitan.utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

import cn.feitan.mediaplayer.PlayBean;
import cn.feitan.mediaplayer.SoundBean;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class MP3Utils {
    public static PlayBean readMP3(SoundBean soundBean,int index){
        PlayBean playBean = new PlayBean();
        playBean.setId(index + 1);

        //读取音频文件
        File file = new File(soundBean.getFilePath());
        //解析文件
        MP3File mp3File = null;
        try {
            mp3File = new MP3File(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        //获取MP3文件的头信息
        MP3AudioHeader audioHeader = (MP3AudioHeader)mp3File.getAudioHeader();
        //获取字符串形式的时长：
        String strLength = audioHeader.getTrackLengthAsString();
        //转换为int类型的时长
        int intLength = audioHeader.getTrackLength();

        Set<String> keySet = mp3File.getID3v2Tag().frameMap.keySet();
        String songName = null;//歌名
        String artist = null;//演唱者
        String album = null;//专辑名称

        if(keySet.contains("TIT2")){
            songName = mp3File.getID3v2Tag().frameMap.get("TIT2").toString();
        }
        if(keySet.contains("TPE1")){
            artist = mp3File.getID3v2Tag().frameMap.get("TPE1").toString();
        }
        if(keySet.contains("TALB")){
            album = mp3File.getID3v2Tag().frameMap.get("TALB").toString();
        }
//            System.out.println("歌名：" + songName + " 演唱者：" + artist + " 专辑名称：" + album);
        if (songName != null && !songName.equals("null")) {
            songName = songName.substring(songName.indexOf("\"") + 1,songName.lastIndexOf("\""));
        }
        if (artist != null && !artist.equals("null")) {
            artist = artist.substring(artist.indexOf("\"") + 1, artist.lastIndexOf("\""));

        }
        if (album != null && !album.equals("null")) {
            album = album.substring(album.indexOf("\"") + 1,album.lastIndexOf("\""));
        }

        //为PlayBean赋值
        playBean.setSoundName(songName);
        playBean.setArtist(artist);
        playBean.setAlbum(album);
        playBean.setFilePath(soundBean.getFilePath());

        URI uri = file.toURI();
        Media media = new Media(uri.toString()) ;
        MediaPlayer mp = new MediaPlayer(media);

        

        playBean.setMediaPlayer(mp);

        //计算文件大小
        BigDecimal bigDecimal = new BigDecimal(file.length());//文件大小，单位：字节
        BigDecimal result = bigDecimal.divide(new BigDecimal(1024 * 1024),2, RoundingMode.HALF_UP);
        playBean.setLength(result.toString() + " M");//字符串的文件大小

        playBean.setTime(strLength);//字符串时间
        playBean.setTotalSeconds(intLength);//总秒数

        //设置删除图片
        ImageView iv = new ImageView("img/left/laji_2_Dark.png");
        iv.setFitWidth(15);
        iv.setFitHeight(15);

        Label labDelete = new Label("",iv);
        labDelete.setOnMouseEntered(e -> iv.setImage(new Image("img/left/laji_2.png")));
        labDelete.setOnMouseExited(e -> iv.setImage(new Image("img/left/laji_2_Dark.png")));

        labDelete.setAlignment(Pos.CENTER);
        playBean.setLabDelete(labDelete);

        //设置图像
        AbstractID3v2Tag tag = mp3File.getID3v2Tag();
        AbstractID3v2Frame frame = (AbstractID3v2Frame)tag.getFrame("APIC");
        if (frame != null) {
            FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
            byte[] imageData = body.getImageData();
            //将字节数组转换为Image对象
            java.awt.Image image = Toolkit.getDefaultToolkit().createImage(imageData, 0, imageData.length);
            BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
            WritableImage writableImage = SwingFXUtils.toFXImage(bufferedImage, null);
            playBean.setImage(writableImage);
        }
        return playBean;
    }
}

