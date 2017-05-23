package listeners;

import com.github.drapostolos.rdp4j.DirectoryListener;
import com.github.drapostolos.rdp4j.DirectoryPoller;
import com.github.drapostolos.rdp4j.FileAddedEvent;
import com.github.drapostolos.rdp4j.FileModifiedEvent;
import com.github.drapostolos.rdp4j.FileRemovedEvent;
import com.github.drapostolos.rdp4j.InitialContentEvent;
import com.github.drapostolos.rdp4j.InitialContentListener;
import com.github.drapostolos.rdp4j.IoErrorCeasedEvent;
import com.github.drapostolos.rdp4j.IoErrorListener;
import com.github.drapostolos.rdp4j.IoErrorRaisedEvent;
import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

import util.Utilities;

public class StatusLIstener implements DirectoryListener, IoErrorListener, InitialContentListener {

  @Override
  public void fileAdded(FileAddedEvent event) {
      
      FileElement file = event.getFileElement();
      
      System.out.println("Added: " + event.getFileElement() );

      
//      if(file.isDirectory() && file.getName().startsWith("20")){
//    	  System.err.println("Adding " + file.getName() + " to poll");
//          DirectoryPoller dp = event.getDirectoryPoller();
//          dp.addPolledDirectory((PolledDirectory) file);
//      }
  }

  @Override
  public void fileRemoved(FileRemovedEvent event) {
      System.out.println("Removed: " + event.getFileElement());
      FileElement file = event.getFileElement();
      
      event.getDirectoryPoller().getPolledDirectories().remove((PolledDirectory) file);
  }

@Override
  public void fileModified(FileModifiedEvent event) {
      System.out.println("Modified: " + event.getFileElement());
      
  }

@Override
  public void ioErrorCeased(IoErrorCeasedEvent event) {
      System.out.println("I/O error ceased.");
  }

@Override
  public void ioErrorRaised(IoErrorRaisedEvent event) {
      System.out.println("I/O error raised!");
      event.getIoException().printStackTrace();
  }

@Override
  public void initialContent(InitialContentEvent event) {
      System.out.println("initial Content: ^");
  }
}