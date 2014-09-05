/*
 * Vertical Datum Transformation Project
 * DoC/NOAA/National Ocean Service
 * http://vdatum.noaa.gov
 *
 * VDatum is a free software tool being developed jointly by NOAA's National Geodetic
 * Survey (NGS), Office of Coast Survey (OCS), and Center for Operational Oceanographic
 * Products and Services (CO-OPS). VDatum is designed to vertically transform
 * geospatial data among a variety of tidal, orthometric and ellipsoidal vertical
 * datums - allowing users to convert their data from different horizontal/vertical
 * references into a common system and enabling the fusion of diverse geospatial
 * data in desired reference levels.
 *
 * The Vertical Datum Transformation software and its data resides in the public
 * domain and may be used without restriction. NOAA/NOS requested that in any
 * subsequent use of this work, NOAA/NOS be given appropriate acknowledgement. The names
 * NOAA, NOS and/or VDatum, however, may not be used in any advertising or publicity 
 * to endorse or promote any products or commercial entity unless specific written
 * permission is obtained from NOAA/NOS. The user also understands that NOAA/NOS is 
 * not obligated to provide the user with any support, consulting, training or assistance
 * of any kind with regard to the use, operation and performance of this software
 * nor to provide the user with any updates, revisions, new versions or "bug fixes".
 *
 * This program and supporting information is furnished by the Government of the
 * United States of America. The program is distributed "AS IS", WITHOUT ANY WARRANTY
 * OF ANY KIND, express or implied, including but not limited to the warranties of
 * merchantability, fitness for a particular purpose and noninfringement. In no event
 * shall the U.S. Government, the Department of Commerce, NOAA, NOS, and any of
 * their employees, contractors, subcontractors be liable for any claim, damages or
 * other liability resulting from any use of this program.
 *
 * For more information, please visit: http://vdatum.noaa.gov
 */
package vdatum.noaa.gov.pack2go;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Packaging VDatum for the warped wild web.
 *
 * @since 02SEP2014
 * @author Băng.Lê
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
            System.out.println("");
            String vdatumworkfolder = vdatumWorkFolder.getAbsolutePath();
            String zipfolder = vdatumworkfolder;
            while (zipfolder.equalsIgnoreCase(vdatumworkfolder)) {
                System.out.println("Save to: (shall be an empty folder, and shall not be the vdatum working folder. For example, C:\\vdatum_zip)");
                javax.swing.JFileChooser jfcs = new javax.swing.JFileChooser();
                jfcs.setMultiSelectionEnabled(false);
                jfcs.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
                jfcs.setCurrentDirectory(curDir.getParentFile());
                result = jfcs.showSaveDialog(null);
                if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                    zipFolder = jfcs.getSelectedFile();
                    zipfolder = zipFolder.getAbsolutePath();
                }
            }

            System.out.println("[" + zipFolder.getAbsolutePath() + "]");
            System.out.println();
            try {
                String date = new SimpleDateFormat("MMddyyyy").format(new Date());
                //get the newest GEOID model from /core/geoid.inf
                Properties prop = new Properties();
                InputStream istream = new FileInputStream(vdatumWorkFolder.getAbsolutePath() + "\\core\\geoid.inf");
                prop.load(istream);
                String newestGEOID = prop.getProperty("geoid_models");
                newestGEOID = newestGEOID.substring(0, newestGEOID.indexOf(","));
                System.out.println("Most current GEOID model: [" + newestGEOID + "]");
                //vdatum_all.zip
                //System.out.println("Packaging [vdatum_all.zip]..");
                //vdatum_all.zip - everything packaged
                ZipOutputStream vdatumallzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_all.zip")));
                //vdatum.zip - vdatum.bat, vdatum.jar, vdatum.inf and core
                //of VERTCON, NADCON, GEOCON and the newest GEOID
                ZipOutputStream vdatumzos = null;
                Scanner keyboard = new Scanner(System.in);
                System.out.print("Release date in MMddyyyy format: (type d for current date) ");
                String input = keyboard.next();
                if (!input.equalsIgnoreCase("d")) {
                    date = input.trim();
                }
                System.out.println();
                System.out.print("Is this a new software or new core release? (yes/default=n)");
                input = keyboard.next();
                if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
                    vdatumzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum.zip")));
                }
                System.out.println();
                System.out.print("Is this a new GEOID release? (y/default=n)");
                input = keyboard.next();
                boolean isNewGEOIDRelease = false;
                if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
                    isNewGEOIDRelease = true;
                }
                System.out.println();
                System.out.print("Is this a new tidal area, IGLD or boundary polygon release? (yes/default=n)");
                input = keyboard.next();
                ZipOutputStream vdatumregionalzos = null;
                if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
                    vdatumregionalzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_regional" + date + ".zip")));
                }
                System.out.println();
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
                            ZipOutputStream tidalzos = null;
                            if (vdatumregionalzos != null) {
                                tidalzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\" + zipname + ".zip")));
                                System.out.println("Packaging [" + zipname + ".zip]..");
                            }
                            for (int j = 0; j < subfiles.length; j++) {
                                zipFile(subfiles[j], "vdatum\\" + zipname, tidalzos, vdatumallzos, vdatumregionalzos);
                            }
                            //add the vdatumWorkFolder\\tidal_area.inf and vdatumWorkFolder\\tidal_area.dat
                            if (tidalzos != null) {
                                zipFile(new File(vdatumWorkFolder + "\\tidal_area.dat"), "vdatum", tidalzos, null, null);
                                zipFile(new File(vdatumWorkFolder + "\\tidal_area.inf"), "vdatum", tidalzos, null, null);
                                tidalzos.closeEntry();
                                tidalzos.close();
                                System.out.println("Done [" + zipname + ".zip]");
                            }
                        } else {
                            //core folder: NADCON (ncla,nclo,hpgnla,hpgnlo) VERTCON, GEOIDxx, GEOCON, GEOCON11
                            ZipOutputStream nadconzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_NADCON.zip")));
                            //System.out.println("Packaging [vdatum_NADCON.zip]..");
                            for (int j = 0; j < subfiles.length; j++) {
                                if (subfiles[j].isDirectory()) {
                                    String zipname = subfiles[j].getName();
                                    String zipnamelc = zipname.toLowerCase();
                                    //VERTCON
                                    if (zipnamelc.contains("vcn") || zipnamelc.contains("geocon")) {
                                        //e.g. core\\vcn or core\\geocon, core\\geocon11                                            
                                        ZipOutputStream vcnzos = null;
                                        if (zipnamelc.contains("vcn")) {
                                            vcnzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_VERTCON.zip")));
                                            System.out.println("Packaging [vdatum_VERTCON.zip]..");
                                            //add vcn.inf to vdatum_VERTCON.zip
                                            zipFile(new File(vdatumWorkFolder + "\\core\\vcn.inf"), "vdatum\\core", vcnzos, vdatumallzos, vdatumzos);
                                        } else {
                                            vcnzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_" + zipname.toUpperCase() + ".zip")));
                                            System.out.println("Packaging [vdatum_" + zipname.toUpperCase() + ".zip]..");
                                            //add geoconxx.inf to vdatum_GEOCONxx.zip
                                            zipFile(new File(vdatumWorkFolder + "\\core\\" + zipnamelc + ".inf"), "vdatum\\core", vcnzos, vdatumallzos, vdatumzos);
                                        }
                                        File[] vcnfiles = subfiles[j].listFiles();
                                        for (int k = 0; k < vcnfiles.length; k++) {
                                            zipFile(vcnfiles[k], "vdatum\\core\\" + zipname, vcnzos, vdatumallzos, vdatumzos);
                                        }
                                        if (vcnzos != null) {
                                            vcnzos.closeEntry();
                                            vcnzos.close();
                                            if (zipnamelc.contains("vcn")) {
                                                System.out.println("Done [vdatum_VERTCON.zip]");
                                            } else {
                                                System.out.println("Done [vdatum_" + zipname.toUpperCase() + ".zip]");
                                            }
                                        }
                                    } else if (zipname.toLowerCase().contains("geoid")) {
                                        //e.g. core\\geoidxx
                                        ZipOutputStream geoidzos = null;
                                        if (isNewGEOIDRelease) {
                                            geoidzos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFolder + "\\vdatum_" + zipname.toUpperCase() + ".zip")));
                                            System.out.println("Packaging [vdatum_" + zipname.toUpperCase() + ".zip]..");
                                        }
                                        File[] geoidfiles = subfiles[j].listFiles();
                                        for (int k = 0; k < geoidfiles.length; k++) {
                                            if (newestGEOID.equalsIgnoreCase(zipname)) {
                                                zipFile(geoidfiles[k], "vdatum\\core\\" + zipname, geoidzos, vdatumallzos, vdatumzos);
                                            } else {
                                                zipFile(geoidfiles[k], "vdatum\\core\\" + zipname, geoidzos, vdatumallzos, null);
                                            }
                                        }
                                        if (newestGEOID.equalsIgnoreCase(zipname)) {
                                            //add geoidxx.inf to geoidxx.zip
                                            zipFile(new File(vdatumWorkFolder + "\\core\\" + zipname + ".inf"), "vdatum\\core", geoidzos, vdatumallzos, vdatumzos);
                                            //add geoid.inf to geoidxx.zip
                                            zipFile(new File(vdatumWorkFolder + "\\core\\geoid.inf"), "vdatum\\core", geoidzos, vdatumallzos, vdatumzos);
                                        } else {
                                            //add geoidxx.inf to geoidxx.zip
                                            zipFile(new File(vdatumWorkFolder + "\\core\\" + zipname + ".inf"), "vdatum\\core", geoidzos, vdatumallzos, null);
                                            //add geoid.inf to geoidxx.zip
                                            zipFile(new File(vdatumWorkFolder + "\\core\\geoid.inf"), "vdatum\\core", geoidzos, null, null);
                                        }

                                        if (geoidzos != null) {
                                            geoidzos.closeEntry();
                                            geoidzos.close();
                                            System.out.println("Done [vdatum_" + zipname.toUpperCase() + ".zip]");
                                        }
                                    } else {
                                        //nadcon: ncla, nclo, hpgnla, hpgnlo
                                        File[] nadconfiles = subfiles[j].listFiles();
                                        for (int k = 0; k < nadconfiles.length; k++) {
                                            zipFile(nadconfiles[k], "vdatum\\core\\" + zipname, nadconzos, vdatumallzos, vdatumzos);
                                        }
                                        //add nadcon.inf
                                        zipFile(new File(vdatumWorkFolder + "\\core\\" + zipname + ".inf"), "vdatum\\core", nadconzos, vdatumallzos, vdatumzos);
                                    }
                                }// else {
                                //   zipFile(subfiles[j], "vdatum\\" + files[i].getName(), vdatumallzos, vdatumzos, null);
                                //}
                            }
                            nadconzos.closeEntry();
                            nadconzos.close();
                            System.out.println("Done [vdatum_NADCON.zip]");
                        }
                    } else {
                        //tidal_area.dat and such
                        if (files[i].getName().toLowerCase().contains("tidal_area")) {
                            zipFile(new File(vdatumWorkFolder + "\\" + files[i].getName()), "vdatum", vdatumallzos, vdatumregionalzos, vdatumzos);
                        } else {
                            zipFile(new File(vdatumWorkFolder + "\\" + files[i].getName()), "vdatum", vdatumallzos, vdatumzos, null);
                        }
                    }
                }
                //
                vdatumallzos.closeEntry();
                vdatumallzos.close();
                System.out.println("Done [vdatum_all.zip]");
                if (vdatumzos != null) {
                    vdatumzos.closeEntry();
                    vdatumzos.close();
                    System.out.println("Done [vdatum.zip]");
                }
                if (vdatumregionalzos != null) {
                    vdatumregionalzos.closeEntry();
                    vdatumregionalzos.close();
                    System.out.println("Done [vdatum_regional" + date + ".zip]");
                }
                System.out.println("All done!");
            } catch (Exception e) {
                e.printStackTrace();
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
