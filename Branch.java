package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/** Represents a gitlet Branch object.
 *  @author Enran Wu
 */
public class Branch implements Serializable {

    /** Name of the Branch. */
    private String name;

    /** Current Commit, represented by UID, the Branch is referring to. */
    private String currCommit;

    /** Initiates a Branch object with given name and given UID of current Commit. */
    public Branch(String name, String currCommitId) {
        this.name = name;
        this.currCommit = currCommitId;
    }

    /** Writes the Branch to the given File with its name. */
    public void writeTo(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        Utils.writeObject(file, this);
    }

    /** Returns the name of the Branch. */
    public String getName() {
        return name;
    }

    /** Finds the Branch with given name. */
    public static Branch findBranch(String branchName) {
        File branchNameDIR = Utils.join(Repository.BRANCHES, branchName);
        return Utils.readObject(branchNameDIR, Branch.class);
    }

    /** Finds the Branch with given file. */
    public static Branch findBranch(File file) {
        return Utils.readObject(file, Branch.class);
    }

    /** Returns the name of current Commit. */
    public String getCurrCommit() {
        return currCommit;
    }

    /** Sets the current commit to the given Commit. */
    public void setCurrCommit(String currCommit) {
        this.currCommit = currCommit;
    }

    /** Checks if the Branch is the sames as the given Branch. */
    public boolean equals(Branch otherBranch) {
        String otherName = otherBranch.getName();
        return otherName.equalsIgnoreCase(this.name);
    }
}
