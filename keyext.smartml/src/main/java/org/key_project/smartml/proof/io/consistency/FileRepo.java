/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.proof.io.consistency;

import org.key_project.smartml.proof.io.RuleSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public interface FileRepo {
    /**
     * Provides access to a file on disk.
     *
     * May return <code>null</code> if the path cannot be handled by this repository.
     *
     * @param path the path of the file
     * @return an InputStream of the requested file, or <code>null</code>
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException on IO errors, e.g. if the user has no permission to read the file
     */
    InputStream getInputStream(Path path) throws FileNotFoundException, IOException;

    /**
     * Provides access to the InputStream of a RuleSource. The file the RuleSource is read from is
     * registered to the FileRepo.
     *
     * May return <code>null</code> if the source cannot be handled by this repository.
     *
     * @param ruleSource the RuleSource
     * @return an InputStream of the RuleSource, or <code>null</code>
     * @throws IOException on IO errors
     */
    InputStream getInputStream(RuleSource ruleSource) throws IOException;

    /**
     * Provides access to the InputStream of a file identified by a URL. The file is registered to
     * the FileRepo.
     *
     * May return <code>null</code> if the url cannot be handled by this repository.
     *
     * @param url the URL of the file
     * @return an InputStream to the file identified by the URL, or <code>null</code>
     * @throws IOException on IO errors
     */
    InputStream getInputStream(URL url) throws IOException;

    /**
     * This method can be used to write a file that has no counterpart outside to the FileRepo.
     *
     * @param path the path of the file to store. The path must be relative to the base directory of
     *        the proof package.
     * @return an OutputStream to the file in the FileRepo
     * @throws FileNotFoundException if a file with the given path exists
     */
    OutputStream createOutputStream(Path path) throws FileNotFoundException;

    /**
     * Register the proof in the FileRepo.
     *
     * @param proof the proof to register
     */
    // void registerProof(Proof proof);

    /**
     * Sets the Rust path (where the source files are located).
     *
     * @param smartmlPath the java path to set (the method does nothing if null is given)
     * @throws IllegalStateException if the Rust path is already set
     */
    void setSmartMLPath(String smartmlPath) throws IllegalStateException;

    /**
     * Sets the base directory of the proof, i.e. the main directory where the proof is loaded from.
     * When loading Rust sources this is the directory the loaded file resides in. When loading
     * .key-Files this is the directory specified via "\\programSource" or the directory of the
     * .key-File, if no source directory is specified.
     *
     * This is needed by the FileRepo for resolving pathnames.
     *
     * @param path The path of the base directory. If a file is given, then its parent directory is
     *        set as base path.
     */
    void setBaseDir(Path path);
}
