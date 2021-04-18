package net.progressit.scriptz.compressui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class CompressBO {
	
	private final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Path.class, new PathToStringConverter()).setPrettyPrinting().create();
	
	@Data
	public static class CompressLogEvent{
		private final String message;
	}
	
	@Data
	public static class CompKeepItem{
		private final Path retainedPathRelativeToRoot;
	}
	@Data
	public static class CompDelItem{
		private final Path retainedPathRelativeToRoot;
		private final Path deletedPathRelativeToRoot;
	}
	@Data
	public static class CompKeepLog{
		private final List<CompKeepItem> itemsToKeep;
	}
	@Data
	public static class CompDelLog{
		private final List<CompDelItem> itemsDeleted;
	}
	
	@Data
	public static class PathPair{
		private final Path keepParent;
		private final Path deleteParent;
	}
	
	@Data
	public static class CompressionPlan{
		private final Path folderToCompress;
		private final Multimap<PathPair, CompressionDetails> folderFilesToCompress = MultimapBuilder.linkedHashKeys().arrayListValues().build();
		public void addInFolder(PathPair folders, CompressionDetails fileToCompress) {
			folderFilesToCompress.put(folders, fileToCompress);
		}
	}
	
	@Data @Builder
	public static class CompressionDetails{
		private final File retainedFile;
		private final File deletedCopy;
		private final Path retainedPathRelativeToRoot;
		private final Path deletedPathRelativeToRoot;
		private final long length;
		private final String murmurHash;
	}
	
	private final EventBus bus;
	public CompressBO(EventBus bus) {
		this.bus = bus;
	}

	/**
	 * Need to upgrade the analysis, so that the compression logs etc are not analyzed.
	 * 
	 * @param folderToCompress
	 * @param bus
	 * @return
	 */
	public CompressionPlan analyze(File folderToCompress) {
		PlanningFileVisitor visitor = new PlanningFileVisitor( folderToCompress.toPath(), bus );
		try {
			java.nio.file.Files.walkFileTree(folderToCompress.toPath(), visitor);
		} catch (IOException e) {
			post("** ERROR **: " + e.toString());
			throw new RuntimeException(e);
		}
		CompressionPlan result = visitor.getPlannedCompression();
		PlanStats stats = visitor.getStats();
		Collection<PathPair> foldersToAct = result.getFolderFilesToCompress().keySet();
		for(PathPair folderToAct:foldersToAct) {
			post("FOLDER: " + folderToAct.getDeleteParent());
			Collection<CompressionDetails> filesToAct = result.getFolderFilesToCompress().get(folderToAct);
			for(CompressionDetails cd:filesToAct) {
				post("PERFORM: Keeping: " + cd.getRetainedPathRelativeToRoot() + " Deleting: " + cd.getDeletedPathRelativeToRoot() + "(" + cd.length + ":" + cd.murmurHash + ")");
			}
		}
		post("RESULT: Scanned: " + sizeText(stats.scannedBytes, 2) + " Deleting: " + sizeText(stats.savedBytes, 2));
		return result;
	}
	
	public void compress(CompressionPlan analysis, boolean dontRetainLogs) {
		post("Starting real File Deletion... ");
		Collection<PathPair> foldersToAct = analysis.getFolderFilesToCompress().keySet();
		for(PathPair folderToAct:foldersToAct) {
			post("FOLDER: " + folderToAct.getDeleteParent());
			Collection<CompressionDetails> filesToAct = analysis.getFolderFilesToCompress().get(folderToAct);
			CompKeepLog keepLog = dontRetainLogs?null:loadCompKeepLog(analysis.folderToCompress, folderToAct.keepParent);
			CompDelLog delLog = dontRetainLogs?null:loadCompDelLog(analysis.folderToCompress, folderToAct.deleteParent);
			for(CompressionDetails pair:filesToAct) {
				try {
					post("Deleting..: " + pair.deletedCopy.getAbsolutePath());
					boolean success = pair.deletedCopy.delete();
					if(!success) pair.deletedCopy.deleteOnExit();
					if(!dontRetainLogs) {
						keepLog.itemsToKeep.add( new CompKeepItem( pair.retainedPathRelativeToRoot ) );
						delLog.itemsDeleted.add( new CompDelItem(pair.retainedPathRelativeToRoot, pair.deletedPathRelativeToRoot) );
					}
				}catch(Exception e) {
					post("** ERROR **: Failed to delete: " + pair.deletedCopy);
				}
			}
			if(!dontRetainLogs && keepLog.getItemsToKeep().size()>0) {
				saveCompKeepLog(analysis.folderToCompress, folderToAct.keepParent, keepLog);
			}
			if(!dontRetainLogs && delLog.getItemsDeleted().size()>0) {
				saveCompDelLog(analysis.folderToCompress, folderToAct.deleteParent, delLog);
			}
		}
		post("Completed File Deletion. ");
	}

	public void decompress(File logToDecompress) {
		
	}
	
	private CompKeepLog loadCompKeepLog(Path folderToCompress, Path keepParent ) {
		File folderToOpen = folderToCompress.resolve(keepParent).toFile();
		File fileToOpen = new File(folderToOpen, "comp-keep.clog.json");
		if(!fileToOpen.exists()) return new CompKeepLog( new ArrayList<>() );
		try( FileReader fr = new FileReader(fileToOpen) ){
			CompKeepLog keepLog = gson.fromJson(fr, CompKeepLog.class);
			return keepLog;
		} catch (IOException e) {
			post("** ERROR **: " + e.toString());
			throw new RuntimeException(e);
		}
	}
	private CompDelLog loadCompDelLog(Path folderToCompress, Path delParent ) {
		File folderToOpen = folderToCompress.resolve(delParent).toFile();
		File fileToOpen = new File(folderToOpen, "comp-remove.clog.json");
		if(!fileToOpen.exists()) return new CompDelLog( new ArrayList<>() );
		try( FileReader fr = new FileReader(fileToOpen) ){
			CompDelLog delLog = gson.fromJson(fr, CompDelLog.class);
			return delLog;
		} catch (IOException e) {
			post("** ERROR **: " + e.toString());
			throw new RuntimeException(e);
		}
	}
	private void saveCompKeepLog(Path folderToCompress, Path keepParent, CompKeepLog keepLog) {
		File folderToOpen = folderToCompress.resolve(keepParent).toFile();
		File fileToOpen = new File(folderToOpen, "comp-keep.clog.json");
		try( FileWriter fw = new FileWriter(fileToOpen) ){
			gson.toJson(keepLog, fw);
		} catch (IOException e) {
			post("** ERROR **: " + e.toString());
			throw new RuntimeException(e);
		}
	}
	private void saveCompDelLog(Path folderToCompress, Path delParent, CompDelLog delLog) {
		File folderToOpen = folderToCompress.resolve(delParent).toFile();
		File fileToOpen = new File(folderToOpen, "comp-remove.clog.json");
		try( FileWriter fw = new FileWriter(fileToOpen) ){
			gson.toJson(delLog, fw);
		} catch (IOException e) {
			post("** ERROR **: " + e.toString());
			throw new RuntimeException(e);
		}
	}
	
	private void post(String msg) {
		bus.post( new CompressLogEvent(msg) );
	}

	@Data
	public static class FileMinimalKey{
		private final String filename;
		private final long length;
	}
	@Data
	public static class FileDetails{
		private final File file;
		private final Path pathRelativeToLog;
		private final String filename;
		private final long length;
		private String murmurHash; //Load on demand.
	}
	@Data
	public static class PlanStats{
		private long scannedBytes;
		private long savedBytes;
		private long filesProcessed;
	}
	
	private static class PlanningFileVisitor implements FileVisitor<Path> {

		private final Multimap<FileMinimalKey, FileDetails> keysToDetails = MultimapBuilder.hashKeys().arrayListValues().build();
		private final Path startFolder;
		@Getter
		private final CompressionPlan plannedCompression;
		private final EventBus bus;
		@Getter
		private final PlanStats stats = new PlanStats();
		public PlanningFileVisitor(Path startFolder, EventBus bus) {
			this.startFolder = startFolder;
			plannedCompression = new CompressionPlan(startFolder);
			this.bus = bus;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (attrs.isSymbolicLink()) {
				System.out.format("Symbolic link: %s ", file);
				return FileVisitResult.CONTINUE;
			} else if (attrs.isRegularFile()) {

			} else {
				System.out.format("Other: %s ", file);
			}

			FileMinimalKey minimalKey = new FileMinimalKey(file.getFileName().toString(), attrs.size());
			stats.scannedBytes += minimalKey.length;
			stats.filesProcessed++;
			if(minimalKey.length<2000) return FileVisitResult.CONTINUE; //Too small to keep checking.
			/*
			if(stats.filesProcessed % 1000 == 999) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
			
			post("Visiting: " + file);
			Collection<FileDetails> mappedFiles = keysToDetails.get(minimalKey);
			if(mappedFiles!=null && mappedFiles.size()>0) {
				String oriAbsPath = file.toFile().getAbsolutePath();
				String fileHash = hash(file.toFile());
				if(fileHash==null) return FileVisitResult.CONTINUE; //Hash unavailable keep checking.
				Path thisFileRelativePath = startFolder.relativize(file);
				boolean foundMatch =false;
				for(FileDetails match:mappedFiles) {
					String matchAbsPath = match.getFile().getAbsolutePath();
					if(!oriAbsPath.equals(matchAbsPath)) { //Not links to the same file.
						ensureHash(match);
						if(match.murmurHash==null) continue; //No hash available for comparison.
						
						if(fileHash.equals(match.murmurHash)) {
							Path matchedFileRelativePath = startFolder.relativize(match.file.toPath());
							CompressionDetails cd = new CompressionDetails.CompressionDetailsBuilder() //
									.retainedFile(match.file) //
									.deletedCopy(file.toFile()) //
									.retainedPathRelativeToRoot(matchedFileRelativePath) //
									.deletedPathRelativeToRoot(thisFileRelativePath) //
									.length( minimalKey.length ) //
									.murmurHash(fileHash) //
									.build();
							
							foundMatch = true;
							stats.savedBytes += cd.length;
							Path keepParent = matchedFileRelativePath.getParent();
							Path delParent = thisFileRelativePath.getParent();
							plannedCompression.addInFolder( new PathPair(keepParent, delParent) , cd); //Only plan this file. Don't note it down as a known file, as its going to be deleted.
							post("PLAN: Keeping: " + cd.getRetainedPathRelativeToRoot() + " Deleting: " + cd.getDeletedPathRelativeToRoot() + "(" + cd.length + ":" + cd.murmurHash + ")");
							break; //No need to continue checking with other files.
						}
					}
				}
				if(!foundMatch) {
					post("Hash mismatch (" + minimalKey.length + ": " + fileHash + " NOW: " + oriAbsPath);
					FileDetails details = new FileDetails(file.toFile(), startFolder.relativize(file), minimalKey.filename, minimalKey.length);
					keysToDetails.put(minimalKey, details);
				}
			}else {
				FileDetails details = new FileDetails(file.toFile(), startFolder.relativize(file), minimalKey.filename, minimalKey.length);
				keysToDetails.put(minimalKey, details);
			}
			
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			if (exc instanceof FileSystemLoopException) {
				System.out.println("cycle detected: " + file);
			} else {
				System.out.format("Unable to read:" + " %s: %s%n", file, exc);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}
		
		private void ensureHash(FileDetails details) {
			if(details.murmurHash == null || details.murmurHash.trim().length()==0) {
				details.murmurHash = hash(details.file);
			}
		}

		private String hash(File file) {
			HashCode hash;
			try {
				hash = Files.asByteSource(file).slice(0, 20_000_000l).hash( Hashing.murmur3_128() ); //Limit hash to first 20MB of the file. Don't hash entire movies.
			} catch (IOException e) {
				System.err.println(e.toString());
				return null;
			}
			String myChecksum = hash.toString().toUpperCase();
			return myChecksum;
		}
		private void post(String msg) {
			bus.post( new CompressLogEvent(msg) );
		}
	}
	
	private String sizeText(double bytes, int decimals) {
		if(bytes<1000d) {
			decimals = 0;
			return String.format("%."+decimals+"f bytes", bytes);
		}else if(bytes<1e6d) {
			decimals = 0;
			return String.format("%."+decimals+"f KB", bytes/1e3d);
		}else if(bytes<1e9d) {
			decimals = Math.min(decimals, 1);
			return String.format("%."+decimals+"f MB", bytes/1e6d);
		}else  {
			decimals = Math.min(decimals, 2);
			return String.format("%."+decimals+"f GB", bytes/1e9d);
		}
	}
}
