/// *
// * Copyright 2015 KPMG N.V. (unless otherwise stated).
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
// package nl.kpmg.lcm.server.backend;
//
// import com.google.common.hash.HashCode;
// import com.google.common.hash.Hashing;
// import com.google.common.io.Files;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.io.InputStream;
// import java.net.URI;
// import java.util.HashMap;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import nl.kpmg.lcm.server.data.Storage;
// import nl.kpmg.lcm.server.data.MetaData;
// import org.apache.commons.io.IOUtils;
// import org.apache.hadoop.conf.Configuration;
// import org.apache.hadoop.fs.FileSystem;
// import org.apache.hadoop.fs.Path;
// import org.junit.AfterClass;
// import org.junit.BeforeClass;
// import org.junit.Test;
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.fail;
//
/// **
// * Test suite for HDP 2.2 Hortonworks Sandbox. The namenode is running on port
// * 8020. If using VirtualBox, make sure that the guest machine network interface
// * is attached to "host-only adapter". The IP of the guest machine can be found
// * using ifconfig command.
// *
// * @see
// * <a href="https://gitlab-nl.dna.kpmglab.com/kave/localcatalogmanager/wikis/accessing-sandbox">
// * wiki</a>
// *
// *
// * It is ignored by default and run only during integration.
// * @author jpavel
// */
// public class BackendHDFSImplIntTest {
//
// /**
// * HDFS server address.
// */
// private static final String TEST_STORAGE_PATH = "hdfs://192.168.56.101:8020/";
// /**
// * Temporary directory in which all the test files will exist.
// */
// private static final String TEST_DIR = "temp_test/";
//
// /**
// * Common access tool for all backends.
// */
// private final Storage backendModel;
//
// /**
// * Default constructor.
// */
// public BackendHDFSImplIntTest() {
// backendModel = new Storage();
// backendModel.setId("test");
// backendModel.setOptions(new HashMap());
// backendModel.getOptions().put("storagePath", TEST_STORAGE_PATH);
// }
//
// /**
// * Makes a temporary test directory.
// *
// * @throws Exception if it is not possible to make a test directory.
// */
// @BeforeClass
// public static void setUpClass() throws Exception {
// // make test temp dir and set storage path
// File testDir = new File(TEST_DIR);
// boolean mkdir = testDir.mkdir();
// // make test file
// File testFile = new File(TEST_DIR + "/testFile.csv");
// testFile.createNewFile();
// try (FileWriter writer = new FileWriter(testFile)) {
// final int nLoops = 10;
// for (int i = 0; i < nLoops; i++) {
// writer.write("qwertyuiop");
// writer.write("\n");
// writer.write("asdfghjkl");
// writer.write("\n");
// writer.write("zxcvbnm,!@#$%^&*()_");
// writer.write("\n");
// writer.write("1234567890[][;',.");
// writer.write("\n");
// }
// writer.flush();
// }
// // now create an HDFS test dir and copy the test file
// Process p;
// try {
// p = Runtime.getRuntime().exec("hdfs dfs -mkdir " + TEST_STORAGE_PATH
// + "user/test");
// p.waitFor();
// p = Runtime.getRuntime().exec("hdfs dfs -copyFromLocal " + TEST_DIR
// + "/testFile.csv " + TEST_STORAGE_PATH + "user/test");
// p.waitFor();
// } catch (IOException | InterruptedException ex) {
// Logger.getLogger(BackendHDFSImpl.class.getName()).log(Level.SEVERE, "Cannot access the hdfs at "
// + TEST_STORAGE_PATH, ex);
// }
// if (mkdir) {
// System.out.println("Setup BackendFileTest successful");
// } else {
// System.out.println("Setup BackendFileTest failed");
// }
// }
//
// /**
// * Deletes the temporary test directory and its content, assuming there are
// * no subdirectories.
// */
// @AfterClass
// public static final void tearDownClass() {
// // delete the local directory + contents
// File file = new File(TEST_DIR);
// for (File c : file.listFiles()) {
// c.delete();
// }
// file.delete();
// // delete hdfs directory + contents
// Process p;
// try {
// p = Runtime.getRuntime().exec("hdfs dfs -rm " + TEST_STORAGE_PATH
// + "user/test/*");
// p.waitFor();
// p = Runtime.getRuntime().exec("hdfs dfs -rmdir " + TEST_STORAGE_PATH
// + "user/test");
// p.waitFor();
// } catch (IOException | InterruptedException ex) {
// Logger.getLogger(BackendHDFSImpl.class.getName()).log(Level.SEVERE, "Cannot access the hdfs at "
// + TEST_STORAGE_PATH, ex);
// }
// }
//
// /**
// * Test of getSupportedUriSchema method, of class BackendHDFSImpl.
// */
// @Test
// public final void testGetSupportedUriSchema() {
// System.out.println("getSupportedUriSchema");
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// String expResult = "hdfs";
// String result = testBackend.getSupportedUriSchema();
// assertEquals(expResult, result);
// }
//
// /**
// * Tests if the URI is parsed correctly in {@link BackendHDFSImpl} class.
// *
// * @throws BackendException if it is not possible to parse the URI
// *
// */
// @Test
// public final void testParseUri() throws BackendException {
// System.out.println("parseUri");
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// String uri = TEST_STORAGE_PATH + "user/name/temp.csv";
// URI dataUri = testBackend.parseUri(uri);
// String filePath = dataUri.getPath();
// System.out.println(filePath);
// assertEquals("/user/name/temp.csv", filePath);
// }
//
// /**
// * Tests if the exception is thrown if wrong URI scheme is parsed.
// *
// * @throws BackendException if it works correctly.
// *
// */
// @Test(expected = BackendException.class)
// public final void testParseFileUri() throws BackendException {
// System.out.println("parseFileUri");
// String server = "file:///";
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// String uri = server + "user/name/temp.csv";
// URI dataUri = testBackend.parseUri(uri);
// // If we got here, we failed to convince the parseUri to throw an exception
// fail("parseUri did not thrown BackendException!");
// }
//
// /**
// * Tests if class can "touch" existing file. Fails if it is not possible.
// *
// * @throws IOException when hdfs connection fails.
// */
// @Test
// public final void testAccess() throws IOException {
// System.out.println("testAcess");
// URI uri = URI.create(TEST_STORAGE_PATH + "user/test/testFile.csv");
// Configuration conf = new Configuration();
// String server = (String) backendModel.getOptions().get("storagePath");
// conf.set("fs.default.name", server);
// FileSystem file = FileSystem.get(conf);
// System.out.println("get file " + file.exists(new Path(uri)));
// assertEquals(true, file.exists(new Path(uri)));
// }
//
// /**
// * Tests what happens if {@link BackendHDFSImp} gathers information using
// * empty {@link MetaData} object. Exception is expected.
// *
// * @throws BackendException if empty metadata are supplied.
// */
// @Test(expected = BackendException.class)
// public final void testGatherDatasetInformationEmptyMetadata() throws BackendException {
// System.out.println("testGatherDatasetInformationEmptyMetadata");
// MetaData metaData = new MetaData();
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
// fail("testGatherDatasetInformationEmptyMetadata did not thrown BackendException!");
// }
//
// /**
// * Tests what happens if {@link BackendHDFSImp} gathers information using
// * {@link MetaData} object with invalid URI.
// *
// * @throws BackendException if empty metadata are supplied.
// */
// @Test
// public final void testGatherDatasetInformationWrongMetadata() throws BackendException {
// System.out.println("testGatherDatasetInformationWrongMetadata");
// MetaData metaData = new MetaData();
// final String fileUri = "NotAnUri";
// metaData.setDataUri(fileUri);
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
// assertEquals(dataSetInformation.isAttached(), false);
// }
//
// /**
// * Tests what happens if {@link BackendHDFSImp} gathers information using
// * {@link MetaData} object with valid URI pointing to non-existing location.
// * The {@link DataSetInformation} object should has isAttached() method
// * equal to false.
// *
// * @throws BackendException if it is not possible to gather information
// * about the dataset
// * @throws IOException if it is not possible to get path of the storage
// * directory
// */
// @Test
// public final void testGatherDatasetInformationWrongLink() throws BackendException, IOException {
// System.out.println("testGatherDatasetInformationWrongLink");
// MetaData metaData = new MetaData();
// final String fileUri = TEST_STORAGE_PATH + "NoFile";
// metaData.setDataUri(fileUri);
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
// assertEquals(dataSetInformation.isAttached(), false);
// }
//
// /**
// * Tests what happens if {@link BackendHDFSImp} gathers information using
// * {@link MetaData} object with valid URI pointing to existing location. The
// * {@link DataSetInformation} object should has isAttached() method equal to
// * true.
// *
// * @throws BackendException if it is not possible to gather information
// * about the dataset
// * @throws IOException if it is not possible to get path of the storage
// * directory
// */
// @Test
// public final void testGatherDatasetInformation() throws BackendException, IOException {
// System.out.println("testGatherDatasetInformation");
// MetaData metaData = new MetaData();
// final String fileUri = TEST_STORAGE_PATH + "user/test/testFile.csv";
// metaData.setDataUri(fileUri);
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
// System.out.println(dataSetInformation.getModificationTime());
// System.out.println(dataSetInformation.isReadable());
// System.out.println(dataSetInformation.getByteSize());
// assertEquals(dataSetInformation.isAttached(), true);
// }
//
// /**
// * Tests store() method of {@link BackendHDFSImp}. Test tries to store text
// * file created during setup, then it downloads back and finally tests if it
// * is identical to the original using md5.
// *
// * @throws BackendException if it is not possible to gather information
// * about the dataset
// * @throws IOException if it is not possible to read from the local file
// */
// @Test
// public final void testStore() throws IOException, BackendException {
// //first make a test file with some content
//
// // now make a metadata with uri
// final String fileUri = TEST_STORAGE_PATH + "user/test/testStore.csv";
// MetaData metaData = new MetaData();
// metaData.setDataUri(fileUri);
// File testFile = new File(TEST_DIR + "/testFile.csv");
// InputStream is = new FileInputStream(testFile);
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// testBackend.store(metaData, is);
// // copy the file back to check that it is ok
// Process p;
// try {
// p = Runtime.getRuntime().exec("hdfs dfs -copyToLocal " + TEST_STORAGE_PATH
// + "user/test/testStore.csv "
// + TEST_DIR + "/testStore.csv");
// p.waitFor();
// } catch (IOException | InterruptedException ex) {
// Logger.getLogger(BackendHDFSImpl.class.getName()).log(Level.SEVERE, "Cannot access the hdfs at "
// + TEST_STORAGE_PATH, ex);
// }
// // check if the files are identical
// final File expected = testFile;
// final File output = new File(TEST_DIR + "/testStore.csv");
// HashCode hcExp = Files.hash(expected, Hashing.md5());
// HashCode hcOut = Files.hash(output, Hashing.md5());
// assertEquals(hcExp.toString(), hcOut.toString());
// }
//
// /**
// * Tests read() method of {@link BackendHDFSImp}. Test reads a text file
// * created by setup and stores in the new text file. Then it tests if the 2
// * files are identical using md5.
// *
// * @throws IOException if there are problems with creating local file or
// * writing to it
// * @throws BackendException if it is not possible to read from the test
// * backend
// */
// @Test
// public final void testRead() throws IOException, BackendException {
// // make a metadata with uri
// final String fileUri = TEST_STORAGE_PATH + "user/test/testFile.csv";
// // make test file to where the content stored in setup would be read
// File output = new File(TEST_DIR + "/testRead.csv");
// output.createNewFile();
// MetaData metaData = new MetaData();
// metaData.setDataUri(fileUri);
// // make local data backend in specified directory and read the existing file
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// try (InputStream is = testBackend.read(metaData)) {
// try (FileOutputStream fos = new FileOutputStream(output)) {
// int readBytes = IOUtils.copy(is, fos);
// Logger.getLogger(BackendHDFSImpl.class.getName())
// .log(Level.INFO, "{0} bytes read", readBytes);
// fos.flush();
// }
// }
// // check if the files are identical
// File testFile = new File(TEST_DIR + "/testFile.csv");
// final File expected = testFile;
// HashCode hcExp = Files.hash(expected, Hashing.md5());
// HashCode hcOut = Files.hash(output, Hashing.md5());
// assertEquals(hcExp.toString(), hcOut.toString());
// }
//
// /**
// * Tests delete() method of {@link BackendHDFSImp}. It tries to store a file
// * on HDFS backend and then delete it. It fails if it is not possible to
// * delete the file.
// *
// * @throws BackendException if it is not possible to delete on the test
// * backend
// */
// @Test
// public final void testDelete() throws BackendException {
// final String fileUri = TEST_STORAGE_PATH + "user/test/testDelete.csv";
// // create a file to be deleted
// Process p;
// try {
// p = Runtime.getRuntime().exec("hdfs dfs -copyFromLocal "
// + TEST_DIR + "/testFile.csv " + TEST_STORAGE_PATH + "user/test/testDelete.csv");
// p.waitFor();
// } catch (IOException | InterruptedException ex) {
// Logger.getLogger(BackendHDFSImpl.class.getName()).log(Level.SEVERE, "Cannot access the hdfs at "
// + TEST_STORAGE_PATH, ex);
// }
// // make metadata pointing to the file to be deleted
// MetaData metaData = new MetaData();
// metaData.setDataUri(fileUri);
// // make local data backend in specified directory and delete the existing file
// BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
// boolean result = testBackend.delete(metaData);
// assertEquals(result, true);
// }
// }
