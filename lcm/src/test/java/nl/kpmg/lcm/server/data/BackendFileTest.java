 /*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.kpmg.lcm.server.data;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import nl.kpmg.lcm.server.metadata.MetaData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;



/**
 *
 * @author Pavel Jez
 */


public class BackendFileTest {

    /**
     * Temorary directory in which all the test files will exist.
     */
    private static final String TEST_STORAGE_PATH = "temp_test/";

    /**
     * Makes a temporary test directory.
     * @throws Exception if it is not possible to make a test directory.
     */
    @Before
    public final void setUp() throws Exception {
        // make test temp dir and set storage path
        File testDir = new File(TEST_STORAGE_PATH);
        testDir.mkdir();
    }

    /**
     * Deletes the temporary test directory and its content, assuming there are
     *  no subdirectories.
     */
    @After
    public final void tearDown() {
        File file = new File(TEST_STORAGE_PATH);
         for (File c : file.listFiles()) {
            c.delete();
         }
        file.delete();
    }

    /**
     * Test to check if "file" URI scheme is supported by getSupportedUriSchema()
     * method.
     */
    @Test
    public final void testGetSupportedUriSchema() {
        File testDir = new File(TEST_STORAGE_PATH);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        String testSchema =  testBackend.getSupportedUriSchema();
        assertEquals("file", testSchema);
    }

    /**
     * Tests if the URI is parsed correctly in {@link BackendFileImpl} class.
     * 
     * @throws BackendException if it is not possible to parse the URI
     * @throws IOException if it is not possible to get full canonical path of a storage location.
     */
    @Test
    public final void testParseUri() throws BackendException, IOException {
        File testDir = new File(TEST_STORAGE_PATH);
        String uri = "file://" + testDir.getCanonicalPath() + "/temp.csv";
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        URI dataUri = testBackend.parseUri(uri);
        String filePath = dataUri.getPath();
        assertEquals(testDir.getCanonicalPath() + "/temp.csv", filePath);
    }

    /**
     * Tests what happens if {@link BackendFileImp} gathers information using empty
     * {@link MetaData} object. Exception is expected.
     *
     * @throws BackendException if empty metadata are supplied.
     */
    @Test(expected = BackendException.class)
    public final void testGatherDatasetInformationEmptyMetadata() throws BackendException {
        MetaData metaData = new MetaData();
        File testDir = new File(TEST_STORAGE_PATH);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
    }

    /**
     * Tests what happens if {@link BackendFileImp} gathers information using
     * {@link MetaData} object with invalid URI. Exception is expected.
     * 
     * @throws BackendException if invalid URI is supplied in metadata
     */
    @Test(expected = BackendException.class)
    public final void testGatherDatasetInformationWrongMetadata() throws BackendException {
        MetaData metaData = new MetaData();
        final String fileUri = "NotAnUri";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        File testDir = new File(TEST_STORAGE_PATH);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
    }

    /**
     * Tests what happens if {@link BackendFileImp} gathers information using
     * {@link MetaData} object with valid URI pointing to non-existing location.
     * The {@link DataSetInformation} object should has isAttached() method equal to false.
     * 
     * @throws BackendException if it is not possible to gather information about the
     *         dataset
     * @throws IOException if it is not possible to get path of the storage directory
     */
    @Test
    public final void testGatherDatasetInformationWrongLink() throws BackendException, IOException {
        MetaData metaData = new MetaData();
        File testDir = new File(TEST_STORAGE_PATH);
        // need to make sure that test file does not exist
        File testFile = new File(TEST_STORAGE_PATH + "/temp.csv");
        testFile.delete();
        final String fileUri = "file://" + testDir.getCanonicalPath() + "/temp.csv";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.isAttached(), false);
    }

    /**
     * Tests what happens if {@link BackendFileImp} gathers information using
     * {@link MetaData} object with valid URI pointing to existing location.
     * The {@link DataSetInformation} object should has isAttached() method equal to true.
     * 
     * @throws BackendException if it is not possible to gather information about the
     *         dataset
     * @throws IOException if it is not possible to get path of the storage directory
     */
    @Test
    public final void testGatherDatasetInformation() throws BackendException, IOException {
        MetaData metaData = new MetaData();
        File testDir = new File(TEST_STORAGE_PATH);
        File testFile = new File(TEST_STORAGE_PATH + "/temp.csv");
        if (!testFile.exists()) {
            new FileOutputStream(testFile).close();
        }
        Date expTimestamp = new Date(testFile.lastModified());
        final String fileUri = "file://" + testDir.getCanonicalPath() + "/temp.csv";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.getModificationTime(), expTimestamp);
    }

    /**
     * Tests store() method of {@link BackendFileImp}.
     * Test creates a text file, then tries to store it and finally tests if it
     * is identical using md5.
     * 
     * @throws BackendException if it is not possible to gather information about the
     *         dataset
     * @throws IOException if it is not possible to get path of the storage directory
     */
    @Test
    public final void testStore() throws IOException, BackendException {
        //first make a test file with some content
        File testFile = new File(TEST_STORAGE_PATH + "/testFile.csv");
        testFile.createNewFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            final int nLoops = 10;
            for (int i = 0; i  < nLoops; i++) {
                writer.write("qwertyuiop");
                writer.write("\n");
                writer.write("asdfghjkl");
                writer.write("\n");
                writer.write("zxcvbnm,!@#$%^&*()_");
                writer.write("\n");
                writer.write("1234567890[][;',.");
                writer.write("\n");
            }
            writer.flush();
        }
        // now make a metadata with uri
        File testDir = new File(TEST_STORAGE_PATH);
        final String fileUri = "file://" + testDir.getCanonicalPath() + "/testStore.csv";
        MetaData metaData = new MetaData();
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        InputStream is = new FileInputStream(testFile);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        testBackend.store(metaData, is);
        final File expected = testFile;
        final File output = new File(testDir.getCanonicalPath() + "/testStore.csv");
        HashCode hcExp = Files.hash(expected, Hashing.md5());
        HashCode hcOut = Files.hash(output, Hashing.md5());
        assertEquals(hcExp.toString(), hcOut.toString());
    }

    /**
     * Tests store() method of {@link BackendFileImp}.
     * Test creates a text file, then tries to store it and modify it afterwards.
     * Finally tests if files are different using md5.
     * 
     * @throws BackendException if it is not possible to gather information about the
     *         dataset
     * @throws IOException if it is not possible to get path of the storage directory
     */
    @Test
    public final void testStore2() throws IOException, BackendException {
        //first make a test file with some content
        File testFile = new File(TEST_STORAGE_PATH + "/testFile2.csv");
        testFile.createNewFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            final int nLoops = 10;
            for (int i = 0; i  < nLoops; i++) {
                writer.write("qwertyuiop");
                writer.write("\n");
                writer.write("asdfghjkl");
                writer.write("\n");
                writer.write("zxcvbnm,!@#$%^&*()_");
                writer.write("\n");
                writer.write("1234567890[][;',.");
                writer.write("\n");
            }
            writer.flush();
        }
        // now make a metadata with uri
        File testDir = new File(TEST_STORAGE_PATH);
        final String fileUri = "file://" + testDir.getCanonicalPath() + "/testStore2.csv";
        MetaData metaData = new MetaData();
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        InputStream is = new FileInputStream(testFile);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        testBackend.store(metaData, is);
        try (FileWriter writer = new FileWriter(testFile, true)) {
            writer.write("\n");
            writer.flush();
        }
        final File expected = testFile;
        final File output = new File(testDir.getCanonicalPath() + "/testStore2.csv");
        HashCode hcExp = Files.hash(expected, Hashing.md5());
        HashCode hcOut = Files.hash(output, Hashing.md5());
        assertNotSame(hcExp.toString(), hcOut.toString());
    }
}

