package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  The structure of a Gitlet Repository is as follows:
 *
 *  .gitlet/ -- top level folder for all persistent data
 *        - head/
 *        - commits/ -- folder with commits made
 *        - blobs/ -- folder with the persistent data for files in the commits
 *        - staging_area/ -- folder with the persistent data files ready to be commit
 *            - removed/
 *            - added/
 *        - branches/
 *
 * @author Enran Wu
 * Note: inspired by lab6 capersRepository
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** Text file containing name of the HEAD branch. */
    static final File HEAD = Utils.join(GITLET_DIR, "head.txt");

    /** Denotes HEAD. */
    static String head;

    /** Folder containing all Commit objects made. */
    static final File COMMITS = Utils.join(GITLET_DIR, "commits");

    /** Folder containing all Blob objects made. */
    static final File BLOBS = Utils.join(GITLET_DIR, "blobs");

    /** Folder containing all Blob objects staged or ready to be committed. */
    static final File STAGING_AREA = Utils.join(GITLET_DIR, "staging_area");

    /** Text file containing Stage objects that are being removed. */
    static final File REMOVED = Utils.join(STAGING_AREA, "removals.txt");

    /** Text file containing Stage objects that are being added. */
    static final File ADDED = Utils.join(STAGING_AREA, "additions.txt");

    /** Folder containing all Branch objects made. */
    static final File BRANCHES = Utils.join(GITLET_DIR, "branches");

    /** Does required filesystem operations to allow for persistence. */
    public static void setupPersistence() throws IOException {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }

        if (!COMMITS.exists()) {
            COMMITS.mkdir();
        }

        if (!BLOBS.exists()) {
            BLOBS.mkdir();
        }

        if (!STAGING_AREA.exists()) {
            STAGING_AREA.mkdir();
        }

        if (!HEAD.exists()) {
            HEAD.createNewFile();
        }

        if (!ADDED.exists()) {
            ADDED.createNewFile();
        }

        if (!REMOVED.exists()) {
            REMOVED.createNewFile();
        }

        if (!BRANCHES.exists()) {
            BRANCHES.mkdir();
        }
    }

    /** Updates the Stages. */
    public static void updateStages(Stage addStage, Stage rmStage) {
        addStage.writeTo(ADDED);
        rmStage.writeTo(REMOVED);
    }

    /** Finds the current Commit the head is on. */
    public static Commit findCurrCommit() {
        head = Utils.readContentsAsString(HEAD);
        Branch currBranch = Branch.findBranch(head);
        Commit currCommit = Commit.findCommit(currBranch.getCurrCommit());
        return currCommit;
    }

    /** Finds the current Branch the head is on. */
    public static Branch findCurrBranch() {
        head = Utils.readContentsAsString(HEAD);
        Branch currBranch = Branch.findBranch(head);
        return currBranch;
    }

    /** Checks if the Stages is empty. */
    public static Boolean isEmpty(Stage addStage, Stage rmStage) {
        return addStage.isEmpty() && rmStage.isEmpty();
    }

    /** Clears the Stages. */
    public static void clear(Stage addStage, Stage rmStage) {
        addStage.clear();
        rmStage.clear();
    }

    /** Prints everything inside the given ArrayList line by line. */
    public static void printLineByLine(ArrayList<String> stringArrayList) {
        for (String each: stringArrayList) {
            System.out.println(each);
        }
    }

    /** Prints everything inside the given Set line by line. */
    public static void printLineByLine(Set<String> stringSet) {
        for (String each: stringSet) {
            System.out.println(each);
        }
    }

    /** Gets the full UID of the Commit given the shorten version of the UID
     * i.e. less than 40 characters.
     */
    public static String getFullId(String shortId) {
        for (String commitId: Utils.plainFilenamesIn(COMMITS)) {
            if (commitId.substring(0, shortId.length()).equals(shortId)) {
                shortId = commitId;
            }
        }
        return shortId;
    }
}
