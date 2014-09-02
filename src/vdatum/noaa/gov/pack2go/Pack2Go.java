package vdatum.noaa.gov.pack2go;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Packaging VDatum for releasing at vdatum website.
 * 
 *
 * @author Bang.Le
 */
public class Pack2Go {

    private static final int BUFFER = 2048;
    private static File vdatumWorkFolder;
    private static File zipFolder;

    public static void main(String[] args) {
        File curDir = null;
        System.out.println("Usage:");
        System.out.println("Packaging vdatum for release on the www, where:");
        System.out.println("  vdatum_all.zip contains all files;");
        System.out.println("  vdatum.zip     including vdatum.jar, vdatum.bat, vdatum.inf and the core folder;");
        System.out.println("  vdatum_geoidxx.zip, vdatum_NADCON.zip, vdatum_vcn.zip;");
        System.out.println("  <tidal_folder>.zip such as FLsouth01_8301.zip ");
        System.out.println("");
        System.out.println("The source folder shall contain all files, as a vdatum working folder.");
        System.out.println("");
        System.out.println("Source folder: (for example, C:\\vdatum)");

        javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        jfc.setCurrentDirectory(null);
        int result = jfc.showOpenDialog(null);
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            curDir = jfc.getSelectedFile();
            vdatumWorkFolder = jfc.getSelectedFile();
            System.out.println("[" + vdatumWorkFolder.getAbsolutePath() + "]");
            System.out.println("Save to: (for example, C:\\vdatum_zip)");
            javax.swing.JFileChooser jfcs = new javax.swing.JFileChooser();
            jfcs.setMultiSelectionEnabled(false);
            jfcs.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
            jfcs.setCurrentDirectory(curDir);
            result = jfcs.showSaveDialog(null);
            if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                zipFolder = jfcs.getSelectedFile();
                System.out.println("[" + zipFolder.getAbsolutePath() + "]");
                try {
                    //vdatum_all.zip
                    System.out.println("Packaging [vdatum_all.zip]..");
                    //vdatum_all.zip - everything packaged
                    ZipOutputStream vdatumallzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_all.zip")));
                    //vdatum.zip - vdatum.bat, vdatum.jar, vdatum.inf and core
                    ZipOutputStream vdatumzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum.zip")));
                    // get a list of files from current directory
                    File[] files = vdatumWorkFolder.listFiles();

                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isDirectory()) {
                            //tidal, core, IGLD85
                            boolean isTidal = false;
                            File[] subfiles = files[i].listFiles();
                            for (int j = 0; j < subfiles.length; j++) {
                                if (subfiles[j].getName().toLowerCase().contains("mhhw")) {
                                    isTidal = true;
                                    break;
                                }
                            }
                            if (isTidal || files[i].getName().toLowerCase().contains("igld")) {
                                String zipname = files[i].getName();//e.g., FLsouth01_8301
                                System.out.println("Packaging [" + zipname + ".zip]..");
                                ZipOutputStream tidalzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\" + zipname + ".zip")));
                                for (int j = 0; j < subfiles.length; j++) {
                                    zipFile(subfiles[j], "vdatum\\" + zipname, tidalzos, vdatumallzos, null);
                                }
                                //add the vdatumWorkFolder\\tidal_area.inf and vdatumWorkFolder\\tidal_area.dat
                                zipFile(new File(vdatumWorkFolder + "\\tidal_area.dat"), "vdatum", tidalzos, null, null);
                                zipFile(new File(vdatumWorkFolder + "\\tidal_area.inf"), "vdatum", tidalzos, null, null);
                                tidalzos.closeEntry();
                                tidalzos.close();
                            } else {
                                //core folder: all subfolders
                                ZipOutputStream nadconzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_NADCON.zip")));
                                for (int j = 0; j < subfiles.length; j++) {
                                    if (subfiles[j].isDirectory()) {
                                        String zipname = subfiles[j].getName();
                                        if (zipname.toLowerCase().contains("geoid") || zipname.toLowerCase().contains("vcn")) {
                                            //e.g. core\\geoidxx or core\\vcn
                                            ZipOutputStream geoidzos = null;
                                            if (zipname.toLowerCase().contains("vcn")) {
                                                System.out.println("Packaging [vdatum_VERTCON.zip]..");
                                                geoidzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_VERTCON.zip")));
                                            } else {
                                                System.out.println("Packaging [vdatum_" + zipname + ".zip]..");
                                                geoidzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_" + zipname + ".zip")));
                                            }
                                            File[] geoidfiles = subfiles[j].listFiles();
                                            for (int k = 0; k < geoidfiles.length; k++) {
                                                zipFile(geoidfiles[k], "vdatum\\" + files[i].getName() + "\\" + zipname, geoidzos, vdatumallzos, vdatumzos);
                                            }
                                            //add geoidxx.inf to geoidxx.zip
                                            zipFile(new File(vdatumWorkFolder + "\\" + files[i].getName() + "\\" + zipname + ".inf"), "vdatum\\" + files[i].getName(), geoidzos, null, null);
                                            zipFile(new File(vdatumWorkFolder + "\\" + files[i].getName() + "\\geoid.inf"), "vdatum\\" + files[i].getName(), geoidzos, null, null);
                                            geoidzos.closeEntry();
                                            geoidzos.close();
                                        } else if (subfiles[j].isFile()) {
                                            zipFile(subfiles[j], "vdatum\\" + files[i].getName(), vdatumzos, vdatumallzos, null);
                                        } else {
                                            //nadcon
                                            File[] nadconfiles = subfiles[j].listFiles();
                                            for (int k = 0; k < nadconfiles.length; k++) {
                                                zipFile(nadconfiles[k], "vdatum\\" + files[i].getName() + "\\" + zipname, nadconzos, vdatumallzos, vdatumzos);
                                            }
                                            //add nadcon.inf
                                            zipFile(new File(vdatumWorkFolder + "\\" + files[i].getName() + "\\" + zipname + ".inf"), "vdatum\\" + files[i].getName(), nadconzos, null, null);
                                        }
                                    } else {
                                        zipFile(subfiles[j], "vdatum\\" + files[i].getName(), vdatumallzos, vdatumzos, null);
                                    }
                                }
                                nadconzos.closeEntry();
                                nadconzos.close();
                            }
                        } else {
                            //tidal_area.dat and such
                            if (files[i].getName().toLowerCase().contains("tidal_area")) {
                                zipFile(new File(vdatumWorkFolder + "\\" + files[i].getName()), "vdatum", vdatumallzos, null, null);
                            } else {
                                zipFile(new File(vdatumWorkFolder + "\\" + files[i].getName()), "vdatum", vdatumallzos, vdatumzos, null);
                            }
                        }
                    }
                    //
                    vdatumallzos.closeEntry();
                    vdatumallzos.close();
                    vdatumzos.closeEntry();
                    vdatumzos.close();
                    System.out.println("Done!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Could either be: vdatum\\FLsouth01_8301\\mllw.gtx vdatum\\core\\geoidxx
     * vdatum\\tidal_area.dat
     *
     * @param origfile the original file to be compressed
     * @param zipname FLsouth01_8301 or vdatum_geoidxx or null
     * @param zipparent1
     * @param zos1
     * @param zipparent2
     * @param zos2
     * @param zipparent3
     * @param zos3
     * @throws Exception
     */
    private static void zipFile(final File origfile, String zipparent,
            final ZipOutputStream zos1,
            final ZipOutputStream zos2,
            final ZipOutputStream zos3) throws Exception {

        byte[] data = new byte[BUFFER];
        System.out.println("Adding [" + origfile.getAbsolutePath() + "]..");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(origfile), BUFFER);
        String zippath = zipparent + "\\" + origfile.getName();//e.g, mllw.gtx

        if (zos1 != null) {
            zos1.putNextEntry(new ZipEntry(zippath));//vdatum\\FLsouth01_8301\\mllw.gtx
        }
        if (zos2 != null) {
            zos2.putNextEntry(new ZipEntry(zippath));//vdatum\\core\\geoidxx\\geoidxx.gtx
        }
        if (zos3 != null) {
            zos3.putNextEntry(new ZipEntry(zippath));//vdatum\\tidal_area.dat
        }

        int len;
        while ((len = bis.read(data, 0, BUFFER)) > 0) {

            if (zos1 != null) {
                zos1.write(data, 0, len);
            }
            if (zos2 != null) {
                zos2.write(data, 0, len);
            }
            if (zos3 != null) {
                zos3.write(data, 0, len);
            }
        }
        bis.close();
    }
}
