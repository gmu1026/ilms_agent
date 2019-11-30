package agent;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class WatchFolder {
	public boolean watchInfoFile() {
		boolean isDetected = false;
		boolean isRun = true;
		
		WatchService watchService = null;
		try {
			watchService = FileSystems.getDefault().newWatchService();
			
			Path path = Paths.get("C:\\Agent");
			
			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
										StandardWatchEventKinds.ENTRY_MODIFY);
			
				SendAgent sendAgent = new SendAgent();
				sendAgent.createHardWareInfo();
				
	            while(isRun) {
                    WatchKey watchKey = null;
					try {
						watchKey = watchService.take();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
                    
	                List<WatchEvent<?>> events = watchKey.pollEvents();
	                for(WatchEvent<?> event : events) {
	                    Kind<?> kind = event.kind();
	                    
	                    Path paths = (Path)event.context();
	                    
	                    String fileName = paths.toString();
	                    if ("info.txt".equals(fileName)) {
	                    	if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
	                    		isRun = false;
	                    		isDetected = true;
	                    		watchService.close();
	                    	} else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
	                    		isRun = false;
	                    		isDetected = true;
	                    		watchService.close();
	                    	}
	                    }
	                }
	            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (watchService != null) {
					watchService.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return isDetected;
	}
}
