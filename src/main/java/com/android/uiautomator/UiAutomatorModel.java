/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.uiautomator;

import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.UiHierarchyXmlLoader;
import com.android.uiautomator.tree.UiNode;
import com.android.uiautomator.tree.BasicTreeNode.IFindNodeListener;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;

import java.io.File;

public class UiAutomatorModel {

    private static UiAutomatorModel inst = null;

    private File mScreenshotFile, mXmlDumpFile;
    private UiAutomatorViewer mView;
    private Image mScreenshot;
    private BasicTreeNode mRootNode;
    private BasicTreeNode mSelectedNode;
    private Rectangle mCurrentDrawingRect;

    // determines whether we lookup the leaf UI node on mouse move of screenshot image
    private boolean mExploreMode = true;

    private UiAutomatorModel(UiAutomatorViewer view) {
        mView = view;
    }

    public static UiAutomatorModel createInstance(UiAutomatorViewer view) {
        if (inst != null) {
            throw new IllegalStateException("instance already created!");
        }
        inst = new UiAutomatorModel(view);
        return inst;
    }

    public static UiAutomatorModel getModel() {
        if (inst == null) {
            throw new IllegalStateException("instance not created yet!");
        }
        return inst;
    }

    public File getScreenshotFile() {
        return mScreenshotFile;
    }

    public File getXmlDumpFile() {
        return mXmlDumpFile;
    }

    public boolean loadScreenshotAndXmlDump(File screenshotFile, File xmlDumpFile) {
        if (screenshotFile != null && xmlDumpFile != null
                && screenshotFile.isFile() && xmlDumpFile.isFile()) {
            ImageData[] data = null;
            Image img = null;
            try {
                // use SWT's ImageLoader to read png from path
                data = new ImageLoader().load(screenshotFile.getAbsolutePath());
            } catch (SWTException e) {
                e.printStackTrace();
                return false;
            }
            // "data" is an array, probably used to handle images that has multiple frames
            // i.e. gifs or icons, we just care if it has at least one here
            if (data.length < 1) return false;
            BasicTreeNode rootNode = new UiHierarchyXmlLoader().parseXml(xmlDumpFile
                    .getAbsolutePath());
            if (rootNode == null) {
                System.err.println("null rootnode after parsing.");
                return false;
            }
            try {
                // Image is tied to ImageData and a Display, so we only need to create once
                // per new image
                img = new Image(mView.getShell().getDisplay(), data[0]);
            } catch (SWTException e) {
                e.printStackTrace();
                return false;
            }
            // only update screenhot and xml if both are loaded successfully
            if (mScreenshot != null) {
                mScreenshot.dispose();
            }
            mScreenshot = img;
            if (mRootNode != null) {
                mRootNode.clearAllChildren();
            }
            // TODO: we should verify here if the coordinates in the XML matches the png
            // or not: think loading a phone screenshot with a tablet XML dump
            mRootNode = rootNode;
            mScreenshotFile = screenshotFile;
            mXmlDumpFile = xmlDumpFile;
            mExploreMode = true;
            mView.loadScreenshotAndXml();
            return true;
        }
        return false;
    }

    public BasicTreeNode getXmlRootNode() {
        return mRootNode;
    }

    public Image getScreenshot() {
        return mScreenshot;
    }

    public BasicTreeNode getSelectedNode() {
        return mSelectedNode;
    }

    /**
     * change node selection in the Model recalculate the rect to highlight,
     * also notifies the View to refresh accordingly
     *
     * @param node
     */
    public void setSelectedNode(BasicTreeNode node) {
        mSelectedNode = node;
        if (mSelectedNode != null && mSelectedNode instanceof UiNode) {
            UiNode uiNode = (UiNode) mSelectedNode;
            mCurrentDrawingRect = new Rectangle(uiNode.x, uiNode.y, uiNode.width, uiNode.height);
        } else {
            mCurrentDrawingRect = null;
        }
        mView.updateScreenshot();
        if (mSelectedNode != null) {
            mView.loadAttributeTable();
        }
    }

    public Rectangle getCurrentDrawingRect() {
        return mCurrentDrawingRect;
    }

    /**
     * Do a search in tree to find a leaf node or deepest parent node containing the coordinate
     *
     * @param x
     * @param y
     */
    public void updateSelectionForCoordinates(int x, int y) {
        if (mRootNode == null)
            return;
        MinAreaFindNodeListener listener = new MinAreaFindNodeListener();
        boolean found = mRootNode.findLeafMostNodesAtPoint(x, y, listener);
        if (found && listener.mNode != null && !listener.mNode.equals(mSelectedNode)) {
            mView.updateTreeSelection(listener.mNode);
        }
    }

    public boolean isExploreMode() {
        return mExploreMode;
    }

    public void toggleExploreMode() {
        mExploreMode = !mExploreMode;
        mView.updateScreenshot();
    }

    public void setExploreMode(boolean exploreMode) {
        mExploreMode = exploreMode;
    }

    private static class MinAreaFindNodeListener implements IFindNodeListener {
        BasicTreeNode mNode = null;
        @Override
        public void onFoundNode(BasicTreeNode node) {
            if (mNode == null) {
                mNode = node;
            } else {
                if ((node.height * node.width) < (mNode.height * mNode.width)) {
                    mNode = node;
                }
            }
        }
    }
}
