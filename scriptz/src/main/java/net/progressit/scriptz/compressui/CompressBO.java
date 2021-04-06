package net.progressit.scriptz.compressui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class CompressBO {
	
	@Data
	public static class CompressLogEvent{
		private final String message;
	}
	
	@Data
	public static class CompressionLog{
		public Date executionDate;
		private String sourceFolderLocationWas;
		public List<CompressionDetails> details;
		public void addDetail(CompressionDetails row) {
			if(details==null) details = new ArrayList<>();
			details.add(row);
		}
		public List<CompressionDetails> getDetails(){
			if(details==null) details = new ArrayList<>();
			return details;
		}
	}
	
	@Data @Builder
	public static class CompressionDetails{
		private final File retainedFile;
		private final File deletedCopy;
		private final Path retainedPathRelativeToLog;
		private final Path deletedPathRelativeToLog;
		private final long length;
		private final String murmurHash;
	}

	public void compress(File folderToCompress, EventBus bus) {
		PlanningFileVisitor visitor = new PlanningFileVisitor( folderToCompress.toPath(), bus );
		try {
			java.nio.file.Files.walkFileTree(folderToCompress.toPath(), visitor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<CompressionDetails> result = visitor.getPlannedCompression();
		PlanStats stats = visitor.getStats();
		for(CompressionDetails cd:result) {
			String msg = "PERFORM: Keeping: " + cd.getRetainedPathRelativeToLog() + " Deleting: " + cd.getDeletedPathRelativeToLog() + "(" + cd.length + ":" + cd.murmurHash + ")";
			bus.post( new CompressLogEvent( msg ) );
		}
		String msg = "RESULT: Scanned: " + sizeText(stats.scannedBytes, 2) + " Deleting: " + sizeText(stats.savedBytes, 2);
		bus.post( new CompressLogEvent( msg ) );
	}

	public void decompress(File logToDecompress, EventBus bus) {
		
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
		private final List<CompressionDetails> plannedCompression = new ArrayList<>();
		private final EventBus bus;
		@Getter
		private final PlanStats stats = new PlanStats();
		public PlanningFileVisitor(Path startFolder, EventBus bus) {
			this.startFolder = startFolder;
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
			
			String msg = "Visiting: " + file;
			bus.post( new CompressLogEvent( msg ) );
			Collection<FileDetails> mappedFiles = keysToDetails.get(minimalKey);
			if(mappedFiles!=null && mappedFiles.size()>0) {
				String oriAbsPath = file.toFile().getAbsolutePath();
				String fileHash = hash(file.toFile());
				if(fileHash==null) return FileVisitResult.CONTINUE; //Hash unavailable keep checking.
				boolean foundMatch =false;
				for(FileDetails match:mappedFiles) {
					String matchAbsPath = match.getFile().getAbsolutePath();
					if(!oriAbsPath.equals(matchAbsPath)) { //Not links to the same file.
						ensureHash(match);
						if(match.murmurHash==null) continue; //No hash available for comparison.
						
						if(fileHash.equals(match.murmurHash)) {
							CompressionDetails cd = new CompressionDetails.CompressionDetailsBuilder() //
									.retainedFile(match.file) //
									.deletedCopy(file.toFile()) //
									.retainedPathRelativeToLog(startFolder.relativize(match.file.toPath())) //
									.deletedPathRelativeToLog(startFolder.relativize(file)) //
									.length( minimalKey.length ) //
									.murmurHash(fileHash) //
									.build();
							
							foundMatch = true;
							stats.savedBytes += cd.length;
							plannedCompression.add(cd); //Only plan this file. Don't note it down as a known file, as its going to be deleted.
							msg = "PLAN: Keeping: " + cd.getRetainedPathRelativeToLog() + " Deleting: " + cd.getDeletedPathRelativeToLog() + "(" + cd.length + ":" + cd.murmurHash + ")";
							bus.post( new CompressLogEvent( msg ) );
							break; //No need to continue checking with other files.
						}
					}
				}
				if(!foundMatch) {
					msg = "Hash mismatch (" + minimalKey.length + ": " + fileHash + " NOW: " + oriAbsPath;
					bus.post( new CompressLogEvent( msg ) );
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
				hash = Files.asByteSource(file).hash( Hashing.murmur3_128() );
			} catch (IOException e) {
				System.err.println(e.toString());
				return null;
			}
			String myChecksum = hash.toString().toUpperCase();
			return myChecksum;
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
