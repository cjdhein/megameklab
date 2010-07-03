/*
 * MegaMekLab - Copyright (C) 2010
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package megameklab.com.ui.Dropship.Printing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;

import megamek.common.Aero;
import megamek.common.Dropship;
import megamek.common.Pilot;
import megamek.common.TechConstants;
import megameklab.com.util.ImageHelper;
import megameklab.com.util.UnitUtil;

public class PrintAerodyne implements Printable {

    private Dropship dropship = null;
    private ArrayList<Dropship> dropshipList;
    PrinterJob masterPrintJob;

    public PrintAerodyne(ArrayList<Dropship> list, PrinterJob masterPrintJob) {
        dropshipList = list;
        this.masterPrintJob = masterPrintJob;

    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex >= 1) {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        // f.setPaper(this.paper);
        printImage(g2d, pageFormat);
        return Printable.PAGE_EXISTS;
    }

    public void printImage(Graphics2D g2d, PageFormat pageFormat) {
        if (g2d == null) {
            return;
        }

        System.gc();

        g2d.drawImage(ImageHelper.getRecordSheet(dropship), 18, 18, 558, 738, Color.BLACK, null);
        printDropshipImage(g2d, ImageHelper.getFluffImage(dropship, ImageHelper.imageDropship));

        printDropshipData(g2d);
        printArmor(g2d);
        printWeaponsNEquipment(g2d);
        printHeatSinks(g2d);
        printFiringArcHeat(g2d);

        // Armor Pips
        printFrontArmor(g2d, dropship.getOArmor(Aero.LOC_NOSE));
        printLeftArmor(g2d, dropship.getOArmor(Aero.LOC_LWING));
        printRightArmor(g2d, dropship.getOArmor(Aero.LOC_RWING));
        printRearArmor(g2d, dropship.getOArmor(Aero.LOC_AFT));

        // Internal Pips
        printStruct(g2d, dropship.get0SI());

        g2d.scale(pageFormat.getImageableWidth(), pageFormat.getImageableHeight());

    }

    private void printDropshipData(Graphics2D g2d) {
        String dropshipName = dropship.getChassis() + " " + dropship.getModel();

        g2d.setFont(UnitUtil.getNewFont(g2d, dropshipName, true, 180, 10.0f));
        g2d.drawString(dropshipName, 49, 118);

        Font font = UnitUtil.deriveFont(8.0f);
        g2d.setFont(font);

        if ((dropship.getCrew() != null) && !dropship.getCrew().getName().equalsIgnoreCase("unnamed")) {
            Pilot pilot = dropship.getCrew();
            g2d.drawString(pilot.getName(), 270, 120);
            g2d.drawString(String.valueOf(pilot.getGunnery()), 295, 132);
            g2d.drawString(String.valueOf(pilot.getPiloting()), 365, 132);
        }

        g2d.drawString(Integer.toString(dropship.getWalkMP()), 99, 154);
        g2d.drawString(Integer.toString(dropship.getRunMPwithoutMASC()), 99, 165);

        g2d.drawString(String.format("%1$s / %2$s", dropship.getFighterBays().size(), dropship.getSmallCraftBays().size()), 104, 176);
        g2d.drawString(String.format("%1$s / Turn", dropship.getFighterLaunchRate()), 188, 176);

        int tonnage = (int) Math.ceil(dropship.getWeight());

        if (tonnage % 5 != 0) {
            tonnage += 5 - (tonnage % 5);
        }

        g2d.drawString(Integer.toString(tonnage), 177, 132.5f);

        int nextDataLine = 153;
        int startLine = 188;
        int lineFeed = 8;

        switch (dropship.getTechLevel()) {

            case TechConstants.T_INTRO_BOXSET:
                ImageHelper.printCenterString(g2d, "(Intro)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
            case TechConstants.T_IS_TW_NON_BOX:
            case TechConstants.T_IS_TW_ALL:
            case TechConstants.T_CLAN_TW:
                break;
            case TechConstants.T_IS_ADVANCED:
            case TechConstants.T_CLAN_ADVANCED:
                ImageHelper.printCenterString(g2d, "(Advanced)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
            case TechConstants.T_IS_EXPERIMENTAL:
            case TechConstants.T_CLAN_EXPERIMENTAL:
                ImageHelper.printCenterString(g2d, "(Experimental)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
            case TechConstants.T_IS_UNOFFICIAL:
            case TechConstants.T_CLAN_UNOFFICIAL:
                ImageHelper.printCenterString(g2d, "(Unofficial)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
        }

        String techBase = "Inner Sphere";
        if (dropship.isClan()) {
            techBase = "Clan";
        }
        g2d.drawString(techBase, 177, 143.5f);

        if ((dropship.getSource() != null) && (dropship.getSource().trim().length() > 0)) {
            String sourceFluff = "Era: ";
            font = UnitUtil.deriveFont(true, 8.0f);
            g2d.setFont(font);

            g2d.drawString(sourceFluff, 138, nextDataLine);

            font = UnitUtil.getNewFont(g2d, dropship.getSource(), false, 51, 8.0f);

            g2d.setFont(font);

            g2d.drawString(dropship.getSource(), 177, nextDataLine);

        } else {
            String yearFluff = "Year: ";
            font = UnitUtil.deriveFont(true, 8.0f);
            g2d.setFont(font);

            g2d.drawString(yearFluff, 138, nextDataLine);

            font = UnitUtil.deriveFont(8.0f);
            g2d.setFont(font);

            g2d.drawString(String.format("%1$s", dropship.getYear()), 177, nextDataLine);

        }

        // Cost/BV
        g2d.drawString(String.format("%1$,d", dropship.calculateBattleValue(true, true)), 152, 469.2f);

        // Crew data
        g2d.drawString(String.format("%1$s/%1$s", dropship.getLifeBoats(), dropship.getEscapePods()), 335, 592.6f);

        g2d.drawString(String.format("%1$s", dropship.getNCrew()), 283, 562.6f);
        g2d.drawString(String.format("%1$s", dropship.getNPassenger()), 283, 572.6f);
        g2d.drawString(String.format("%1$s", 0), 283, 582.6f);

        g2d.drawString(String.format("%1$s", UnitUtil.getInfBayCount(dropship)), 357, 561.6f);
        g2d.drawString(String.format("%1$s", 0), 357, 571.6f);
        g2d.drawString(String.format("%1$s", UnitUtil.getBattleArmorBayCount(dropship)), 357, 581.6f);

        // myFormatter = new DecimalFormat("#,###.##");
        // g2d.drawString(String.format("%1$,.0f C-bills",
        // dropship.getCost(true)),
        // 52, 346.2f);

        font = new Font("Arial", Font.BOLD, 7);
        g2d.setFont(font);
        g2d.drawString("2010", 50.5f, 740f);
    }

    private void printArmor(Graphics2D g2d) {

        // Armor
        Font font = UnitUtil.deriveFont(true, 9.0f);
        g2d.setFont(font);

        ImageHelper.printCenterString(g2d, String.format("%1$S (%2$s)", dropship.getThresh(Aero.LOC_NOSE), dropship.getArmor(Aero.LOC_NOSE)), g2d.getFont(), 410, 64);
        ImageHelper.printCenterString(g2d, String.format("%1$S (%2$s)", dropship.getThresh(Aero.LOC_RWING), dropship.getArmor(Aero.LOC_RWING)), g2d.getFont(), 545, 305);
        ImageHelper.printCenterString(g2d, String.format("%1$S (%2$s)", dropship.getThresh(Aero.LOC_LWING), dropship.getArmor(Aero.LOC_LWING)), g2d.getFont(), 280, 305);
        ImageHelper.printCenterString(g2d, String.format("%1$S (%2$s)", dropship.getThresh(Aero.LOC_AFT), dropship.getArmor(Aero.LOC_AFT)), g2d.getFont(), 410, 487);
        ImageHelper.printCenterString(g2d, String.format("%1$S", dropship.get0SI()), g2d.getFont(), 446, 234);

    }

    private void printHeatSinks(Graphics2D g2d) {
        Font font = UnitUtil.deriveFont(true, 8.0f);
        g2d.setFont(font);

        int posX = 395;
        int posY = 688;

        // Heat Sinks
        if (dropship.getHeatType() == Aero.HEAT_DOUBLE) {
            g2d.drawString(String.format("%1$s (%2$s)", dropship.getHeatSinks(), dropship.getHeatSinks() * 2), posX, posY);
            g2d.drawString("Double", posX, posY + 8);
        } else {
            g2d.drawString(String.format("%1$s (%1$s)", dropship.getHeatSinks()), posX, posY);
            g2d.drawString("Single", posX, posY + 8);
        }

    }

    private void printFiringArcHeat(Graphics2D g2d) {
        Font font = UnitUtil.deriveFont(true, 8.0f);
        g2d.setFont(font);

        g2d.drawString(String.format("%1$s", dropship.getHeatInArc(Aero.LOC_NOSE, false)), 489, 688);
        g2d.drawString(String.format("%1$s", dropship.getHeatInArc(Aero.LOC_LWING, false)), 489, 703);
        g2d.drawString(String.format("%1$s", dropship.getHeatInArc(Aero.LOC_LWING, true)), 489, 719);

        g2d.drawString(String.format("%1$s", dropship.getHeatInArc(Aero.LOC_AFT, false)), 549, 688);
        g2d.drawString(String.format("%1$s", dropship.getHeatInArc(Aero.LOC_RWING, false)), 549, 703);
        g2d.drawString(String.format("%1$s", dropship.getHeatInArc(Aero.LOC_RWING, true)), 549, 719);
    }

    private void printWeaponsNEquipment(Graphics2D g2d) {

        ImageHelper.printDropshipWeaponsNEquipment(dropship, g2d);

    }

    public void print() {

        try {
            for (int pos = 0; pos < dropshipList.size(); pos++) {
                PrinterJob pj = PrinterJob.getPrinterJob();
                pj.setPrintService(masterPrintJob.getPrintService());
                PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

                aset.add(PrintQuality.HIGH);

                PageFormat pageFormat = new PageFormat();
                pageFormat = pj.getPageFormat(null);

                Paper p = pageFormat.getPaper();
                p.setImageableArea(0, 0, p.getWidth(), p.getHeight());
                pageFormat.setPaper(p);

                pj.setPrintable(this, pageFormat);
                dropship = dropshipList.get(pos);
                pj.setJobName(dropship.getChassis() + " " + dropship.getModel());

                try {
                    pj.print(aset);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.gc();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void printFrontArmor(Graphics2D g2d, int totalArmor) {
        float[] topColumn =
            { 342, 97 };
        float[] pipShift =
            { 7, 7 };
        float maxColumns = 20;

        Vector<float[]> pipPlotter = new Vector<float[]>(200);
        for (int pos = 1; pos <= totalArmor; pos++) {
            // ImageHelper.drawDropshipArmorPip(g2d, topColumn[0],
            // topColumn[1]);
            pipPlotter.add(new float[]
                { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];
            if (pos % maxColumns == 0) {
                topColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                topColumn[0] += pipShift[0];
                /*
                 * if (pos > totalArmor - maxColumns) { topColumn[0] +=
                 * pipShift[0] ((maxColumns - (totalArmor - pos)) / 2); } else {
                 * topColumn[0] += pipShift[0] / 2; }
                 */
            }
        }

        for (int pos = 0; pos < totalArmor; pos++) {
            ImageHelper.drawDropshipArmorPip(g2d, pipPlotter.get(pos)[0], pipPlotter.get(pos)[1]);
            if (--totalArmor <= 0) {
                return;
            }
        }
    }

    private void printRearArmor(Graphics2D g2d, int totalArmor) {
        float[] topColumn = new float[]
            { 366, 300 };
        float[] pipShift = new float[]
            { 6.2f, 6.2f };
        float maxColumns = 15;

        Vector<float[]> pipPlotter = new Vector<float[]>(132);
        for (int pos = 1; pos <= totalArmor; pos++) {
            pipPlotter.add(new float[]
                { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];
            if (pos % maxColumns == 0) {
                topColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                topColumn[0] += pipShift[0];
            }
        }

        for (int pos = 0; pos < totalArmor; pos++) {
            ImageHelper.drawDropshipArmorPip(g2d, pipPlotter.get(pos)[0], pipPlotter.get(pos)[1]);
        }

    }

    private void printLeftArmor(Graphics2D g2d, int totalArmor) {

        float startX = 319.5f;
        float startY = 240f;

        float[] topColumn = new float[]
            { startX, startY };
        float[] pipShift = new float[]
            { 6.2f, 7 };

        int numberPerRow = 5;
        int curretNumber = 0;
        int rowNumber = 1;

        Vector<float[]> pipPlotter = new Vector<float[]>(totalArmor);

        for (int pos = 1; pos < totalArmor; pos++) {
            pipPlotter.add(new float[]
                { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];

            if (++curretNumber == numberPerRow) {
                rowNumber++;
                if (rowNumber == 9) {
                    numberPerRow += 2;
                } else if ((rowNumber == 13) || (rowNumber == 14) || (rowNumber == 15) || (rowNumber == 17) || (rowNumber == 18) || (rowNumber == 20) || (rowNumber == 26) || (rowNumber == 30)) {
                    numberPerRow++;
                    startX -= 6.3f;
                }

                topColumn[1] += pipShift[1];
                topColumn[0] = startX;
                curretNumber = 0;
            }
        }

        for (int pos = 0; pos < totalArmor; pos++) {
            ImageHelper.drawDropshipArmorPip(g2d, pipPlotter.get(pos)[0], pipPlotter.get(pos)[1]);
            if (--totalArmor <= 0) {
                return;
            }
        }
    }

    private void printRightArmor(Graphics2D g2d, int totalArmor) {
        float startX = 499f;
        float startY = 240f;

        float[] topColumn = new float[]
            { startX, startY };
        float[] pipShift = new float[]
            { 6.2f, 7 };

        int numberPerRow = 5;
        int curretNumber = 0;
        int rowNumber = 1;

        Vector<float[]> pipPlotter = new Vector<float[]>(totalArmor);

        for (int pos = 1; pos < totalArmor; pos++) {
            pipPlotter.add(new float[]
                { topColumn[0], topColumn[1] });
            topColumn[0] -= pipShift[0];

            if (++curretNumber == numberPerRow) {
                rowNumber++;
                if (rowNumber == 9) {
                    numberPerRow += 2;
                } else if ((rowNumber == 13) || (rowNumber == 14) || (rowNumber == 15) || (rowNumber == 17) || (rowNumber == 18) || (rowNumber == 20) || (rowNumber == 26) || (rowNumber == 30)) {
                    numberPerRow++;
                    startX += 6.2f;
                }

                topColumn[1] += pipShift[1];
                topColumn[0] = startX;
                curretNumber = 0;
            }
        }

        for (int pos = 0; pos < totalArmor; pos++) {
            ImageHelper.drawDropshipArmorPip(g2d, pipPlotter.get(pos)[0], pipPlotter.get(pos)[1]);
            if (--totalArmor <= 0) {
                return;
            }
        }
    }

    private void printStruct(Graphics2D g2d, int totalArmor) {
        int[] topColumn = new int[]
            { 358, 240 };
        int[] pipShift = new int[]
            { 11, 9 };

        Vector<int[]> pipPlotter = new Vector<int[]>(51);
        for (int pos = 1; pos <= totalArmor; pos++) {
            pipPlotter.add(new int[]
                { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];
            if (pos % 10 == 0) {
                topColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                topColumn[0] += pipShift[0];
            }
        }

        for (int pos = 0; pos < totalArmor; pos++) {
            ImageHelper.drawDropshipISPip(g2d, pipPlotter.get(pos)[0], pipPlotter.get(pos)[1]);
        }
    }

    private void printDropshipImage(Graphics2D g2d, Image img) {

        int width = Math.min(220, img.getWidth(null));
        int height = Math.min(112, img.getHeight(null));
        int drawingX = 18 + ((220 - width) / 2);
        int drawingY = 482 + ((112 - height) / 2);
        g2d.drawImage(img, drawingX, drawingY, width, height, Color.BLACK, null);

    }

}