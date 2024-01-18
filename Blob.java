package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/** Represents a gitlet Blob object.
 *  @author Enran Wu
 */
public class Blob implements Serializable {

    /** Name of the Blob. */
    private String name;

    /** Serialized content of the Blob. */
    private byte[] content;

    /** Initiates a Blob object with given name and given File. */
    public Blob(String name, File file) {
        this.name = name;
        content = Utils.readContents(file);
    }

    /** Returns the Blob's content. */
    public byte[] getContent() {
        return content;
    }

    /** Returns the Blob's name. */
    public String getName() {
        return name;
    }

    /** Returns the SHA-1 UID of the Blob. */
    public String hash() {
        return Utils.sha1((Object) Utils.serialize(this));
    }

    /** Creates a file named Blob's UID with Blob. */
    public void createBlobFile() throws IOException {
        File blobFile = Utils.join(Repository.BLOBS, this.hash());
        if (!blobFile.exists()) {
            blobFile.createNewFile();
        }
        Utils.writeObject(blobFile, this);
    }

    /** Finds the Blob in given file. */
    public static Blob findBlob(File file) {
        return Utils.readObject(file, Blob.class);
    }

    /** Finds the Blob with the given name. */
    public static Blob findBlob(String name) {
        File blobFile = Utils.join(Repository.BLOBS, name);
        return Utils.readObject(blobFile, Blob.class);
    }
}
