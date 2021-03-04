package core;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TransferableImage implements Transferable{

private BufferedImage image;

  public TransferableImage (BufferedImage image) {
    this.image = image;
  }

  @Override
  // Returns supported flavors
  public DataFlavor[] getTransferDataFlavors () {
    return new DataFlavor[] { DataFlavor.imageFlavor };
  }

  @Override
  // Returns true if flavor is supported
  public boolean isDataFlavorSupported (DataFlavor flavor) {
    return DataFlavor.imageFlavor.equals(flavor);
  }

  @Override
  // Returns image
  public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (!DataFlavor.imageFlavor.equals(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    return image;
  }
}
