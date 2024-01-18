package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/** Represents a gitlet Stage Object.
 *  A Stage contains files with Blob's UID; it can be a Stage for adding or removing.
 *  @author Enran Wu
 *  */
public class Stage implements Serializable {

    /** Name of the Stage. */
    private String name;

    /** Files in Stage. */
    private HashMap<String, String> files;

    /** Initiates a Stage Object, */
    public Stage(String name) {
        this.name = name;
        this.files = new HashMap<>();
    }

    /** Gets the name of the Stage. */
    public String getName() {
        return this.name;
    }

    /** Gets the files in the Stage. */
    public HashMap<String, String> getFiles() {
        return this.files;
    }

    /** Adds the given Blob to the Stage. */
    public void add(Blob blob) {
        this.files.put(blob.getName(), blob.hash());
    }

    /** Adds the Blob with the given name to the Stage. */
    public void add(String blobName) {
        this.files.put(blobName, Blob.findBlob(blobName).hash());
    }

    /** Removes the given Blob from the Stage. */
    public void remove(Blob blob) {
        this.files.remove(blob.getName());
    }

    /** Removes the Blob with the given name from the Stage. */
    public void remove(String newName) {
        this.files.remove(newName);
    }

    /** Clears all Blobs from the Stage. */
    public void clear() {
        files.clear();
    }

    /** Writes the Stage to a file. */
    public void writeTo(File file) {
        Utils.writeObject(file, this);
    }

    /** Finds the Stage in File. */
    public static Stage findStage(File file) {
        return Utils.readObject(file, Stage.class);
    }

    /** Checks if the Stage contains given Blob. */
    public boolean contains(Blob blob) {
        return this.files.containsValue(blob.hash());
    }

    /** Checks if the Stage contains Blob with given name. */
    public boolean contains(String newName) {
        return this.files.containsKey(newName);
    }

    /** Checks if the Stage is empty. */
    public boolean isEmpty() {
        return this.files.isEmpty();
    }

    /** Gets the names of all Blobs in Stage. */
    public Set<String> getNames() {
        return this.files.keySet();
    }

    /** Gets the name of the file of the Blob with the given name. */
    public String get(String newName) {
        return this.files.get(newName);
    }

    /** Adds all files in the Stage to the given Commit. */
    public void addTo(Commit commit) {
        for (String blobName: getNames()) {
            commit.getFiles().put(blobName, get(blobName));
        }
    }

    /** Removes all files in the Stage from the given Commit. */
    public void rmFrom(Commit commit) {
        for (String blobName: getNames()) {
            commit.getFiles().remove(blobName, get(blobName));
        }
    }
}
