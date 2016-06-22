package sergii.makarenko.service;

import java.io.IOException;

/**
 * Created by serg on 22.06.16.
 */
public class NangaJavaFXException extends IOException {

    public NangaJavaFXException() {
        super();
    }

    public NangaJavaFXException(String message) {
        super(message);
    }

    public NangaJavaFXException(String message, Throwable cause) {
        super(message, cause);
    }

    public NangaJavaFXException(Throwable cause) {
        super(cause);
    }

}
