package at.fhtw.persistence;


import at.fhtw.models.MediaContent;

public class MediaRepository {

    public void save(MediaContent media) {
        System.out.println(media.getTitle() + " saved to Database");
    }

}
