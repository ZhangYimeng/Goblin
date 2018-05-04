package person.mochi.goblin.lookinto;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.datavec.api.split.BaseInputSplit;
import org.datavec.api.util.files.URIUtil;
import org.nd4j.linalg.collection.CompactHeapStringList;
import org.nd4j.linalg.util.MathUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * File input split. Splits up a root directory in to files.
 *
 * @author Adam Gibson
 */
public class FileSplit extends BaseInputSplit {

	protected File rootDir;
	// Use for Collections, pass in list of file type strings
	protected String[] allowFormat = null;
	protected boolean recursive = true;
	protected Random random;
	protected boolean randomize = false;

	protected FileSplit(File rootDir, String[] allowFormat, boolean recursive, Random random, boolean runMain) {
		System.out.println("初始化FileSplit！");
		this.allowFormat = allowFormat;
		this.recursive = recursive;
		this.rootDir = rootDir;
		if (random != null) {
			this.random = random;
			this.randomize = true;
		}
		if (runMain)
			this.initialize();
	}

	public FileSplit(File rootDir) {
		this(rootDir, null, true, null, true);
	}

	public FileSplit(File rootDir, Random rng) {
		this(rootDir, null, true, rng, true);
	}

	public FileSplit(File rootDir, String[] allowFormat) {
		this(rootDir, allowFormat, true, null, true);
	}

	public FileSplit(File rootDir, String[] allowFormat, Random rng) {
		this(rootDir, allowFormat, true, rng, true);
	}

	public FileSplit(File rootDir, String[] allowFormat, boolean recursive) {
		this(rootDir, allowFormat, recursive, null, true);
	}

	protected void initialize() {
		System.out.println("开始执行FileSplit.initialize()。");
		Collection<File> subFiles;

		if (rootDir == null) {
			throw new IllegalArgumentException("File path must not be null");
		} else if (rootDir.isAbsolute() && !rootDir.exists()) {
			// 如果rootDir是绝对路径并且rootDir不存在,从网上拉取
			System.out.println("rootDir是绝对路径并且rootDir不存在");
			try {
				if (!rootDir.createNewFile()) {
					throw new IllegalArgumentException("Unable to create file " + rootDir.getAbsolutePath());
				}
				// ensure uri strings has the root file if it's not a directory
				else {
					System.out.println("rootDir.createNewFile()方法成功，接下来初始化uriStrings");
					uriStrings = new ArrayList<String>();
					uriStrings.add(rootDir.toURI().toString());
					System.out.println("uriStrings的内容为：\n" + uriStrings);
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		} else if (!rootDir.getAbsoluteFile().exists()) {
			// When implementing wild card characters in the rootDir, remove this if exists,
			// verify expanded paths exist and check for the edge case when expansion cannot
			// be translated to existed locations
			System.out.println("AbsoluteFile不存在。");
			throw new IllegalArgumentException("No such file or directory: " + rootDir.getAbsolutePath());
		} else if (rootDir.isDirectory()) {
			System.out.println("rootDir是一个路径");
			subFiles = new LinkedList<>();
			System.out.println("执行listFiles。");
			listFiles(subFiles, rootDir.toPath(), allowFormat, recursive);
			System.out.println("执行CompactHeapStringList()。");
			uriStrings = new CompactHeapStringList();
			if (randomize) {
				iterationOrder = new int[subFiles.size()];
				for (int i = 0; i < iterationOrder.length; i++) {
					iterationOrder[i] = i;
				}
				MathUtils.shuffleArray(iterationOrder, random);
			}
			for (File f : subFiles) {
				uriStrings.add(URIUtil.fileToURI(f).toString());
				++length;
			}
		} else {
			// Lists one file
			String toString = URIUtil.fileToURI(rootDir).toString(); // URI.getPath(), getRawPath() etc don't have
																		// file:/ prefix necessary for conversion back
																		// to URI
			uriStrings = new ArrayList<>(1);
			uriStrings.add(toString);
			length += rootDir.length();
		}
	}

	@Override
	public String addNewLocation() {
		if (rootDir.isDirectory())
			return addNewLocation(new File(rootDir, UUID.randomUUID().toString()).toURI().toString());
		else {
			// add a file in the same directory as the file with the same extension as the
			// original file
			return addNewLocation(new File(rootDir.getParent(),
					UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(rootDir.getAbsolutePath())).toURI()
							.toString());

		}
	}

	@Override
	public String addNewLocation(String location) {
		File f = new File(URI.create(location));
		try {
			f.createNewFile();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		uriStrings.add(location);
		++length;
		return location;
	}

	@Override
	public void updateSplitLocations(boolean reset) {
		if (reset) {
			initialize();
		}
	}

	@Override
	public boolean needsBootstrapForWrite() {
		return locations() == null || locations().length < 1 || locations().length == 1 && !locations()[0].isAbsolute();
	}

	@Override
	public void bootStrapForWrite() {
		if (locations().length == 1 && !locations()[0].isAbsolute()) {
			File parentDir = new File(locations()[0]);
			File writeFile = new File(parentDir, "write-file");
			try {
				writeFile.createNewFile();
				// since locations are dynamically generated, allow
				uriStrings.add(writeFile.toURI().toString());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}

		}
	}

	@Override
	public OutputStream openOutputStreamFor(String location) throws Exception {
		FileOutputStream ret = location.startsWith("file://") ? new FileOutputStream(new File(URI.create(location)))
				: new FileOutputStream(new File(URI.create(location)));
		return ret;
	}

	@Override
	public InputStream openInputStreamFor(String location) throws Exception {
		FileInputStream ret = location.startsWith("file://") ? new FileInputStream(new File(URI.create(location)))
				: new FileInputStream(new File(URI.create(location)));
		return ret;
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public void reset() {
		if (randomize) {
			// Shuffle the iteration order
			MathUtils.shuffleArray(iterationOrder, random);
		}
	}

	@Override
	public boolean resetSupported() {
		return true;
	}

	public File getRootDir() {
		return rootDir;
	}

	private Collection<File> listFiles(Collection<File> fileNames, Path dir, String[] allowedFormats,
			boolean recursive) {
		IOFileFilter filter;
		if (allowedFormats == null) {
			filter = new RegexFileFilter(".*");
		} else {
			// System.out.println("筛选符合后缀的文件");
			filter = new SuffixFileFilter(allowedFormats);
		}

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path path : stream) {
				if (Files.isDirectory(path) && recursive) {
					// System.out.println("path仍为一个路径，继续迭代");
					listFiles(fileNames, path, allowedFormats, recursive);
				} else {
					if (allowedFormats == null) {
						// System.out.println("如果allowedFormats为空，则任意添加文件至集合。");
						fileNames.add(path.toFile());
					} else {
						if (filter.accept(path.toFile())) {
							// System.out.println("经过filter过滤的文件加入集合");
							fileNames.add(path.toFile());
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println(fileNames);
		return fileNames;
	}
}
