package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/** Represents a gitlet commit object.
 *
 *  @author Enran Wu
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** Time of the Commit. */
    private Date time;

    /** Names of files of the Commit. */
    private HashMap<String, String> files;

    /** Name of the parent Commit. */
    private String parent;

    /** Initiates a Commit object. */
    public Commit(String message, Date time, String parent) {
        this.message = message;
        this.time = time;
        this.parent = parent;
        this.files = new HashMap<>();
    }

    /** Gets the message of the Commit. */
    public String getMessage() {
        return this.message;
    }

    /** Gets the time of the Commit. */
    public Date getTime() {
        return this.time;
    }
    /** Returns the files of the Commit. */
    public HashMap<String, String> getFiles() {
        return this.files;
    }

    /** Gets the names of the files insides the Commit. */
    public Set<String> getNames() {
        return this.files.keySet();
    }

    /** Returns the Blob with given name. */
    public Blob getBlob(String name) {
        String blobHash = this.files.get(name);
        File blobFile = Utils.join(Repository.BLOBS, blobHash);
        return Blob.findBlob(blobFile);
    }

    /** Writes the Commit to the given File. */
    public void writeTo(File file) {
        Utils.writeObject(file, this);
    }

    /** Returns the SHA-1 UID of the Commit. */
    public String hash() {
        return Utils.sha1((Object) Utils.serialize(this));
    }

    /** Sets commit parent to the UID of given Commit. */
    public void setParent(Commit commit) {
        parent = commit.hash();
    }

    /** Finds a Commit with the given File. */
    public static Commit findCommit(File file) {
        return Utils.readObject(file, Commit.class);
    }

    /** Checks if the Commit contains the given Blob. */
    public boolean contains(Blob blob) {
        return files.containsValue(blob.hash());
    }

    /** Checks if the Commit contains Blob with given name. */
    public boolean contains(String name) {
        return files.containsKey(name);
    }

    ///** Adds all files in add Stage to the Commit. */
    //public void addStageToCommit() {
    //    Stage addStage = Stage.findStage(Repository.ADDED);
    //    for (String name: addStage.getNames()) {
    //        this.files.put(name, addStage.get(name));
    //    }
    //}

    ///** Removes all files in remove Stage from the Commit. */
    //public void rmStageFromCommit() {
    //    Stage rmStage = Stage.findStage(Repository.REMOVED);
    //    for (String name: rmStage.getNames()) {
    //        this.files.remove(name, rmStage.get(name));
    //    }
    //}

    /** Creates a commit file with name as commit's UID. */
    public void createCommitFile() throws IOException {
        File commitFile = Utils.join(Repository.COMMITS, hash());
        if (!commitFile.exists()) {
            commitFile.createNewFile();
        }
        Utils.writeObject(commitFile, this);
    }

    /** Gets the parent of the Commit. */
    public String getParent() {
        return this.parent;
    }

    /** Prints out the Commit specify by Commands.log. */
    public void print() {
        String header = "commit " + hash();
        String date;

        //time = getTime();
        date = "Date: " + String.format("%ta %tb %td %tT %tY %tz",
                time, time, time, time, time, time);
        //message = getMessage();

        // printing
        System.out.println("===");
        System.out.println(header);
        System.out.println(date);
        System.out.println(message + "\n");
    }

    /** Finds a Commit with the given UID. */
    public static Commit findCommit(String uid) {
        File commitWithUID = Utils.join(Repository.COMMITS, uid);
        return Utils.readObject(commitWithUID, Commit.class);
    }

    /** Creates a Commit copy of itself with given message. */
    public Commit copy(String newMessage) {
        return new Commit(newMessage, this.time, this.parent);
    }

    /** Gets the UID of the commit. */
    public String getId() {
        return this.hash();
    }
}
