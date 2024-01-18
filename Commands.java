package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/** Available Commands for Gitlet.
 *  Note that the execution of these commands will mainly concerned
 *  with the interaction of Repository directories and files.
 * @author Enran Wu
 */
public class Commands {

    /** Creates a new Gitlet version-control system in the current directory.
     *  This system will automatically start with one commit:
     *  a commit that contains no files and has the commit message initial commit
     *  (just like that, with no punctuation).
     *  It will have a single branch: master, which initially points to this initial commit,
     *  and master will be the current branch.
     * @usage java gitlet.Main init
     */
    public static void init() throws IOException {

        // Exception Case
        if (Repository.GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists "
                    + "in the current directory.");
            System.exit(0);
        }

        // Initiates folders and files
        Repository.setupPersistence();
        Stage addStage = new Stage("Additions");
        Stage rmStage = new Stage("Removals");
        Repository.updateStages(addStage, rmStage);

        // Initiates first commit
        Commit initCommit = new Commit("initial commit", new Date(0), null);
        File initCommitFile = Utils.join(Repository.COMMITS, initCommit.hash());
        if (!initCommitFile.exists()) {
            initCommitFile.createNewFile();
        }
        initCommit.writeTo(initCommitFile);

        // Initiates master branch
        Branch master = new Branch("master", initCommit.hash());
        File masterFile = Utils.join(Repository.BRANCHES, "master");
        if (!masterFile.exists()) {
            masterFile.createNewFile();
        }
        master.writeTo(masterFile);

        // Set head to master branch
        Repository.head = master.getName();
        Utils.writeContents(Repository.HEAD, Repository.head);
    }

    /** Adds a copy of the file as it currently exists to the staging area
     *  (see the description of the commit command). For this reason,
     *  adding a file is also called staging the file for addition.
     *  Staging an already-staged file overwrites the previous entry in the staging area
     *  with the new contents. If the current working version of the file is
     *  identical to the version in the current commit, do not stage it to be added,
     *  and remove it from the staging area if it is already there
     *  (as can happen when a file is changed, added, and then changed back to
     *  it’s original version).
     * @param fileName name of the file
     * @usage java gitlet.Main add [file name]
     */
    public static void add(String fileName) throws IOException {
        File targetFile = Utils.join(Repository.CWD, fileName);

        // Exception Case
        if (!targetFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // Set Up
        Commit currCommit = Repository.findCurrCommit();
        Blob fileBlob = new Blob(fileName, targetFile);
        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);
        rmStage.remove(fileBlob);

        // File is already added
        if (currCommit.contains(fileBlob) && addStage.contains(fileBlob)) {
            addStage.remove(fileBlob);
            Repository.updateStages(addStage, rmStage);
            System.exit(0);
        }

        // File is the same as the one in current commit
        if (currCommit.contains(fileBlob) && !addStage.contains(fileBlob)) {
            Repository.updateStages(addStage, rmStage);
            System.exit(0);
        }

        // File is already removed
        if (!currCommit.contains(fileBlob) && rmStage.contains(fileBlob)) {
            rmStage.remove(fileBlob);
            addStage.add(fileBlob);
            fileBlob.createBlobFile();
            Repository.updateStages(addStage, rmStage);
            System.exit(0);
        }

        // Otherwise
        addStage.add(fileBlob);
        fileBlob.createBlobFile();
        Repository.updateStages(addStage, rmStage);
    }

    /** Saves a snapshot of tracked files in the current commit
     * and staging area so they can be restored at a later time,
     * creating a new commit. The commit is said to be tracking the saved files.
     * By default, each commit’s snapshot of files will be exactly the same as
     * its parent commit’s snapshot of files; it will keep versions of files exactly as they are,
     * and not update them. A commit will only update the contents of files it is tracking
     * that have been staged for addition at the time of commit, in which case the commit
     * will now include the version of the file that was staged instead of the version it
     * got from its parent.
     * @param message message with the commit
     * @usage java gitlet.Main commit [message]
     */
    public static void commit(String message) throws IOException {
        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);

        // Both stages are empty
        if (Repository.isEmpty(addStage, rmStage)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // Blank commit message
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        // Set Up
        Commit currCommit = Repository.findCurrCommit();
        Commit newCommit = currCommit.copy(message);
        newCommit.setParent(currCommit);
        addStage.addTo(newCommit);
        rmStage.rmFrom(newCommit);

        // Checks the cases of equals to current branch
        Branch dummyBranch = new Branch("", "");
        for (String fileName: Utils.plainFilenamesIn(Repository.BRANCHES)) {
            dummyBranch = Branch.findBranch(fileName);
            if (dummyBranch.equals(Repository.findCurrBranch())) {
                break;
            }
        }
        dummyBranch.setCurrCommit(newCommit.hash());

        Repository.clear(addStage, rmStage);
        Repository.updateStages(addStage, rmStage);
        File dummyBranchFile = Utils.join(Repository.BRANCHES, dummyBranch.getName());
        dummyBranch.writeTo(dummyBranchFile);
        newCommit.createCommitFile();
    }

    /** Unstage the file if it is currently staged for addition.
     *  If the file is tracked in the current commit, stage it for removal and
     *  remove the file from the working directory if the user has not already done so
     *  (do not remove it unless it is tracked in the current commit).
     *  @param fileName name of the file
     * @usage java gitlet.Main rm [file name]
     */
    public static void rm(String fileName) {
        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);
        Commit currCommit = Repository.findCurrCommit();

        // Exception
        if ((!addStage.contains(fileName)) && (!currCommit.contains(fileName))) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        // File is added and not tracked in the current commit
        if (addStage.contains(fileName)) {
            addStage.remove(fileName);
            Repository.updateStages(addStage, rmStage);
            System.exit(0);
        }

        // Otherwise i.e. tracked in the current commit
        Blob currBlob = currCommit.getBlob(fileName);
        rmStage.add(currBlob);
        File cwdFile = Utils.join(Repository.CWD, fileName);
        Utils.restrictedDelete(cwdFile);
        Repository.updateStages(addStage, rmStage);
    }

    /** Starting at the current head commit, display information about each commit backwards
     *  along the commit tree until the initial commit, following the first parent commit links,
     *  ignoring any second parents found in merge commits.
     *  (In regular Git, this is what you get with git log --first-parent).
     *  This set of commit nodes is called the commit’s history.
     *  For every node in this history, the information it should display is the commit id,
     *  the time the commit was made, and the commit message.
     * @usage java gitlet.Main log
     */
    public static void log() {
        String currCommitId = Repository.findCurrCommit().getId();

        // Keeps printing long as there is commit exists
        while (currCommitId != null) {
            Stage addStage = Stage.findStage(Repository.ADDED);
            Stage rmStage = Stage.findStage(Repository.REMOVED);
            Commit currCommit = Commit.findCommit(currCommitId);
            currCommit.print();
            currCommitId = currCommit.getParent();
        }
    }

    /** Like log, except displays information about all commits ever made.
     *  The order of the commits does not matter.
     * @usage java gitlet.Main global-log
     */
    public static void globalLog() {

        for (String fileName: Utils.plainFilenamesIn(Repository.COMMITS)) {
            Stage addStage = Stage.findStage(Repository.ADDED);
            Stage rmStage = Stage.findStage(Repository.REMOVED);
            Commit currCommit = Commit.findCommit(fileName);
            currCommit.print();
        }
    }

    /** Prints out the ids of all commits that have the given commit message, one per line.
     *  If there are multiple such commits, it prints the ids out on separate lines.
     *  The commit message is a single operand; to indicate a multiword message,
     *  put the operand in quotation marks, as for the commit command below.
     *  @param message
     * @usage java gitlet.Main find [commit message]
     */
    public static void find(String message) {
        ArrayList<String> ids = new ArrayList<>();

        // Gets IDs of all commits with the given message and stores them in ids
        for (String fileName: Utils.plainFilenamesIn(Repository.COMMITS)) {
            Commit currCommit = Commit.findCommit(fileName);
            String currMessage = currCommit.getMessage();
            String currCommitId = currCommit.hash();
            if (currMessage.equals(message)) {
                ids.add(currCommitId);
            }
        }

        // Exception Case
        if (ids.isEmpty()) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }

        // Prints out all the IDs in ids
        Repository.printLineByLine(ids);
    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     *  Also displays what files have been staged for addition or removal.
     * @usage java gitlet.Main status
     */
    public static void status() {
        Set<String> tracker = new TreeSet<String>();

        // Branches Case
        System.out.println("=== Branches ===");
        for (String fileName: Utils.plainFilenamesIn(Repository.BRANCHES)) {
            Branch dummy = Branch.findBranch(fileName);
            tracker.add(dummy.getName());
        }
        Branch currBranch = Repository.findCurrBranch();
        for (String fileName: tracker) {
            Branch dummy = Branch.findBranch(fileName);
            if (dummy.equals(currBranch)) {
                fileName = "*" + fileName;
            }
            System.out.println(fileName);
        }
        System.out.println();
        tracker.clear();

        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);

        // Staged Files Case
        tracker.addAll(addStage.getNames());
        System.out.println("=== Staged Files ===");
        Repository.printLineByLine(tracker);
        System.out.println();
        tracker.clear();

        // Removed Files Case
        tracker.addAll(rmStage.getNames());
        System.out.println("=== Removed Files ===");
        Repository.printLineByLine(tracker);
        System.out.println();
        tracker.clear();

        // Others
        System.out.println("=== Modifications Not Staged For Commit ===" + "\n");
        System.out.println("=== Untracked Files ===" + "\n");
    }

    /** Takes the version of the file as it exists in the head commit and puts
     * it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * @param fileName
     * @usage java gitlet.Main checkout -- [file name]
     */
    public static void checkout(String fileName) throws IOException {
        Commit currCommit = Repository.findCurrCommit();

        if (!currCommit.contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        // Checkout Process
        Blob target = currCommit.getBlob(fileName);
        byte[] content = target.getContent();
        File file = Utils.join(Repository.CWD, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        Utils.writeContents(file, content);
    }

    /** Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file
     * that’s already there if there is one.
     * The new version of the file is not staged.
     * @param commitID the UID of the Commit, can be shorten
     * @param fileName
     * @usage java gitlet.Main checkout [commit id] -- [file name]
     */
    public static void checkout(String commitID, String fileName) throws IOException {
        if (commitID.length() < 40) {
            commitID = Repository.getFullId(commitID);
        }

        File targetCommitFile = Utils.join(Repository.COMMITS, commitID);

        // Exception Case 1: Invalid Commit ID
        if (!targetCommitFile.exists())  {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);
        Commit targetCommit = Commit.findCommit(targetCommitFile);

        // Exception Case 2: File not exist in commit
        if (!targetCommit.contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        // Checkout Process
        Commit currCommit = Commit.findCommit(targetCommitFile);
        Blob target = currCommit.getBlob(fileName);
        byte[] content = target.getContent();
        File file = Utils.join(Repository.CWD, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        Utils.writeContents(file, content);
    }

    /** Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * At the end of this command, the given branch will now be considered the current
     * branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the
     * checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch.
     * @param branchName Name of the branch wanted to checkout
     * @usage java gitlet.Main checkout [branch name]
     */
    public static void checkoutBranch(String branchName) throws IOException {
        File branchFile = Utils.join(Repository.BRANCHES, branchName);
        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);

        // Exception Case 1
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        Branch currBranch = Repository.findCurrBranch();
        Branch targetBranch = Branch.findBranch(branchName);
        Commit currCommit = Repository.findCurrCommit();
        Commit targetCommit = Commit.findCommit(targetBranch.getCurrCommit());

        // Exception Case 2
        if (targetBranch.equals(currBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Repository.clear(addStage, rmStage);
        Repository.updateStages(addStage, rmStage);

        for (String fileName: targetCommit.getNames()) {
            // Exception Case 3 for untrack files
            File targetFile = Utils.join(Repository.CWD, fileName);
            if (targetFile.exists()) {
                Blob targetBlob = new Blob(fileName, targetFile);
                File blobFile = Utils.join(Repository.BLOBS, targetBlob.hash());
                if (!blobFile.exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            // Checkout Process
            checkout(targetCommit.getId(), fileName);
        }

        // Delete files as necessary
        for (String fileName: currCommit.getNames()) {
            File targetFile = Utils.join(Repository.CWD, fileName);
            if (targetFile.exists() && !targetCommit.contains(fileName)) {
                Utils.restrictedDelete(targetFile);
            }
        }

        // Update branch and head
        targetBranch.writeTo(branchFile);
        Repository.head = targetBranch.getName();
        Utils.writeContents(Repository.HEAD, Repository.head);
    }

    /** Creates a new branch with the given name, and points it at the current head commit.
     * A branch is nothing more than a name for a reference (a SHA-1 identifier)
     * to a commit node.
     * This command does NOT immediately switch to the newly created branch
     * (just as in real Git).
     * Before you ever call branch, your code should be running with a default branch
     * called “master”.
     * @usage java gitlet.Main branch [branch name]
     */
    public static void branch(String branchName) throws IOException {
        File newBranchFile = Utils.join(Repository.BRANCHES, branchName);

        // Exception Case
        if (newBranchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        Branch currBranch = Repository.findCurrBranch();
        Commit currCommit = Repository.findCurrCommit();
        Branch newBranch = new Branch(branchName, currCommit.getId());
        newBranch.writeTo(newBranchFile);
        if (!newBranchFile.exists()) {
            newBranchFile.createNewFile();
        }
    }

    /** Deletes the branch with the given name.
     *  This only means to delete the pointer associated with the branch;
     *  it does not mean to delete all commits that were created under the branch,
     *  or anything like that.
     * @param branchName
     * @usage java gitlet.Main rm-branch [branch name]
     */
    public static void rmBranch(String branchName) {
        File branchFile = Utils.join(Repository.BRANCHES, branchName);

        // Exception Case 1
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit((0));
        }

        // Exception Case 2
        Repository.head = Utils.readContentsAsString(Repository.HEAD);
        Branch currBranch = Branch.findBranch(Repository.head);
        Branch targetBranch = Branch.findBranch(branchName);
        if (targetBranch.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        branchFile.delete();
    }

    /** Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * The command is essentially checkout of an arbitrary commit that also
     * changes the current branch head.
     * @usage java gitlet.Main reset [commit id]
     */
    public static void reset(String commitID) throws IOException {
        if (commitID.length() < 40) {
            commitID = Repository.getFullId(commitID);
        }
        File commitFile = Utils.join(Repository.COMMITS, commitID);
        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);

        // Exception Case 1 for non exist ID
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Repository.clear(addStage, rmStage);
        Repository.updateStages(addStage, rmStage);
        Commit targetCommit = Commit.findCommit(commitID);

        for (String fileName: targetCommit.getNames()) {
            File targetFile = Utils.join(Repository.CWD, fileName);
            // Exception Case 2 for untrack files
            if (targetFile.exists()) {
                Blob targetBlob = new Blob(fileName, targetFile);
                File blobFile = Utils.join(Repository.BLOBS, targetBlob.hash());
                if (!blobFile.exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            // Checkout Process
            checkout(targetCommit.getId(), fileName);
        }

        Commit currCommit = Repository.findCurrCommit();
        Branch currBranch = Repository.findCurrBranch();
        // Delete files as necessary
        for (String fileName: currCommit.getNames()) {
            File targetFile = Utils.join(Repository.CWD, fileName);
            if (targetFile.exists() && !targetCommit.contains(fileName)) {
                Utils.restrictedDelete(targetFile);
            }
        }

        // Update branch and head branch
        currBranch.setCurrCommit(commitID);
        File headBranchFile = Utils.join(Repository.BRANCHES, Repository.head);
        currBranch.writeTo(headBranchFile);
    }

    /** Merges files from the given branch into the current branch.
     * @usage java gitlet.Main merge [branch name]
     * NOTE: currently only implemented exception cases
     */
    public static void merge(String branchName) throws IOException {
        Stage addStage = Stage.findStage(Repository.ADDED);
        Stage rmStage = Stage.findStage(Repository.REMOVED);

        // Exception Case 1: Uncommitted changes
        if (!addStage.isEmpty() || !rmStage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        // Exception Case 2: Nonexistent branch
        File targetBranchFile = Utils.join(Repository.BRANCHES, branchName);
        if (!targetBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        // Exception Case 3: Merge itself
        Branch targetBranch = Branch.findBranch(branchName);
        Branch currBranch = Repository.findCurrBranch();
        if (currBranch.getName().equals(targetBranch.getName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit targetCommit = Commit.findCommit(targetBranch.getCurrCommit());
        // Exception Case 3: Untracked files
        for (String fileName: targetCommit.getNames()) {
            File targetFile = Utils.join(Repository.CWD, fileName);
            if (targetFile.exists()) {
                Blob targetBlob = new Blob(fileName, targetFile);
                File blobFile = Utils.join(Repository.BLOBS, targetBlob.hash());
                if (!blobFile.exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            checkout(targetCommit.getId(), fileName);
        }

        throw new UnsupportedOperationException();
    }
}
