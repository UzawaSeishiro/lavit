/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package lavit.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import lavit.Env;
import lavit.FrontEnd;
import lavit.event.TabChangeListener;
import lavit.frame.FindReplaceDialog;
import lavit.localizedtext.MsgID;
import lavit.multiedit.EditorPage;
import lavit.multiedit.LineColumn;
import lavit.multiedit.TabView;
import lavit.multiedit.coloring.lexer.TokenLabel;
import lavit.multiedit.event.CaretPositionChangeListener;
import lavit.multiedit.event.TabButtonListener;
import lavit.system.FileHistory;
import lavit.util.CommonFontUser;
import lavit.util.FileFilters;
import lavit.util.FileUtils;
import extgui.dialog.AskDialogBuilder;
import extgui.dialog.DialogResult;
import extgui.dialog.MessageType;
import extgui.filedrop.event.FileDropListener;
import extgui.fileview.FileViewPane;
import extgui.fileview.event.FileSelectedListener;

@SuppressWarnings("serial")
public class EditorPanel extends JPanel implements CommonFontUser
{
	public EditorButtonPanel buttonPanel;

	private FileViewPane fileView;
	private TabView tabView;
	private int hlFlags;
	private FileDropListener fileDropListener = new FileDropAction();

	public EditorPanel()
	{
		setLayout(new BorderLayout());

		tabView = new TabView();
		tabView.addTabButtonListener(new TabButtonListener()
		{
			public void closeButtonClicked(int tabIndex)
			{
				tabView.setSelectedPage(tabIndex);
				closeSelectedPage();
			}
		});
		tabView.addTabChangeListener(new TabChangeListener()
		{
			public void tabChanged()
			{
				JTextPane selectedEditor = getSelectedEditor();
				if (selectedEditor != null)
				{
					FindReplaceDialog.getDialog().setTargetTextComponent(selectedEditor);
				}
			}
		});

		final JLabel labelSelLength = new JLabel();
		labelSelLength.setHorizontalAlignment(SwingConstants.RIGHT);
		final JLabel labelLineColumn = new JLabel();
		labelLineColumn.setHorizontalAlignment(SwingConstants.RIGHT);

		tabView.addCaretPositionChangeListener(new CaretPositionChangeListener()
		{
			public void caretPositionChanged(LineColumn lineColumn)
			{
				labelLineColumn.setText(lineColumn.line + " : " + lineColumn.column);
				if (lineColumn.selectionLength == 0)
				{
					labelSelLength.setText("--");
				}
				else
				{
					labelSelLength.setText(String.valueOf(lineColumn.selectionLength));
				}
			}
		});

		JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		panelStatus.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 1, 0, 0, Color.LIGHT_GRAY),
			BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE)));

		GroupLayout gl = new GroupLayout(panelStatus);
		panelStatus.setLayout(gl);
		gl.setHorizontalGroup(gl.createSequentialGroup()
			.addComponent(labelLineColumn, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(labelSelLength, 40, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE)
		);
		gl.setVerticalGroup(gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(labelLineColumn)
			.addComponent(labelSelLength)
		);

		JPanel panelEditor = new JPanel(new BorderLayout());
		panelEditor.add(tabView, BorderLayout.CENTER);
		panelEditor.add(panelStatus, BorderLayout.SOUTH);

		loadFont();
		FrontEnd.addFontUser(this);

		fileView = new FileViewPane(panelEditor);
		fileView.addFileSelectedListener(new FileSelectedListener()
		{
			public void fileSelected(File selectedFile)
			{
				openFile(selectedFile);
			}
		});
		add(fileView, BorderLayout.CENTER);

		buttonPanel = new EditorButtonPanel(this);
		add(buttonPanel, BorderLayout.SOUTH);

		setMinimumSize(new Dimension(0, 0));

		updateHighlight();

		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, mask), "mag_font");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, mask), "mag_font");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, mask), "mag_font");

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mask), "min_font");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, mask), "min_font");

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, mask), "def_font");

		ActionMap am = getActionMap();
		am.put("mag_font", getScaleUpAction());
		am.put("min_font", getScaleDownAction());
		am.put("def_font", getScaleDefaultAction());
	}

	public void updateHighlight()
	{
		String colortarget = Env.get("COLOR_TARGET");
		hlFlags = 0;
		for (String target : colortarget.split("\\s+"))
		{
			if (target.equals("comment"))
			{
				hlFlags |= TokenLabel.COMMENT;
				hlFlags |= TokenLabel.STRING;
			}
			else if (target.equals("symbol"))
			{
				hlFlags |= TokenLabel.OPERATOR;
			}
			else if (target.equals("reserved"))
			{
				hlFlags |= TokenLabel.KEYWORD;
			}
			else if (target.equals("rulename"))
			{
				hlFlags |= TokenLabel.RULENAME;
			}
		}
		for (EditorPage page : tabView.getPages())
		{
			page.removeAllHighlights();
			page.addHighlight(hlFlags);
			page.setShowTabs(Env.is("SHOW_TABS"));
			page.setShowEols(Env.is("SHOW_LINE_DELIMITERS"));
		}
		repaint();
	}

	public JTextPane getSelectedEditor()
	{
		EditorPage page = tabView.getSelectedPage();
		return page != null ? page.getJTextPane() : null;
	}

	/**
	 * 選択されているページが存在しない場合は{@code null}を返す。
	 */
	public File getFile()
	{
		EditorPage page = tabView.getSelectedPage();
		return page != null ? page.getFile() : null;
	}

	public List<File> getFiles()
	{
		List<File> files = new ArrayList<File>();
		EditorPage[] pages = tabView.getPages();
		for (EditorPage page : pages)
		{
			if (page.hasFile())
			{
				files.add(page.getFile());
			}
		}
		return files;
	}

	/**
	 * Returns a number of opend tabs.
	 */
	public int getTabCount()
	{
		return tabView.getTabCount();
	}

	public void addTabChangeListener(TabChangeListener l)
	{
		tabView.addTabChangeListener(l);
	}

	public void setFileViewVisible(boolean b)
	{
		fileView.setFileViewVisible(b);
	}

	public boolean isFileViewVisible()
	{
		return fileView.isFileViewVisible();
	}

	public void setFileViewDividerLocation(int location)
	{
		fileView.setDividerLocation(location);
	}

	public int getFileViewDividerLocation()
	{
		return fileView.getDividerLocation();
	}

	public void setFileViewExtensionFilterText(String filterText)
	{
		fileView.setExtensionFilterText(filterText);
	}

	public String getFileViewExtensionFilterText()
	{
		return fileView.getExtensionFilterText();
	}

	/**
	 * first.lmn を開く
	 * first.lmn が存在しなければ作成する。
	 */
	public void firstFileOpen()
	{
		File first = new File("first.lmn").getAbsoluteFile();
		if (!first.exists())
		{
			try
			{
				first.createNewFile();
				FrontEnd.println("(EDITOR) new file.");
			}
			catch (Exception e)
			{
				FrontEnd.printException(e);
			}
		}
		openFile(first);
	}

	/**
	 * 前回開いていたファイルを開く
	 */
	public void openInitialFiles()
	{
		List<File> files = Env.loadLastFiles();
		if (!files.isEmpty())
		{
			openFiles(files);
			if (tabView.getTabCount() > 0)
			{
				tabView.setSelectedIndex(0);
			}
		}
		else
		{
			firstFileOpen();
		}
	}

	/**
	 * 新規作成
	 */
	public void newFileOpen()
	{
		EditorPage page = createEmptyPage();
		tabView.setSelectedPage(page);
		FrontEnd.println("(EDITOR) new file.");
	}

	/**
	 * 開くファイルを選択する。
	 */
	//TODO: このパネルの仕事ではない。
	public void fileOpen()
	{
		File file = chooseOpenFile();
		if (file != null)
		{
			openFile(file);
		}
	}

	/**
	 * ファイルを開く。
	 */
	public void openFile(File file)
	{
		String encoding = Env.get("EDITOR_FILE_READ_ENCODING");
		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), encoding));

			StringBuilder buf = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1)
			{
				buf.append(Character.toChars(c));
			}
			reader.close();

			EditorPage page = createPage(file, buf.toString());
			page.setFile(file);
			tabView.setSelectedPage(page);

			FrontEnd.println("(EDITOR) file open. [ " + file.getName() + " ]");
		}
		catch (Exception e)
		{
			FrontEnd.printException(e);
		}

		FileHistory.get().add(file);
	}

	/**
	 * 上書き保存
	 */
	public boolean fileSave()
	{
		EditorPage page = tabView.getSelectedPage();
		try
		{
			if (page.hasFile())
			{
				editorFileSave(page.getFile());
				return true;
			}
			else
			{
				return fileSaveAs();
			}
		}
		catch (IOException e)
		{
			FrontEnd.printException(e);
		}
		return false;
	}

	/**
	 * 名前を付けて保存
	 */
	public boolean fileSaveAs()
	{
		File file = chooseWriteFile();
		if (file != null)
		{
			try
			{
				editorFileSave(file);
				return true;
			}
			catch (IOException e)
			{
				FrontEnd.printException(e);
			}
		}
		return false;
	}

	private void openFiles(Collection<File> files)
	{
		for (File file : files)
		{
			if (file.exists() && file.isFile())
			{
				openFile(file);
			}
		}
	}

	private EditorPage createPage(File file, String text)
	{
		EditorPage page = createIfNotOpened(file);
		page.setText(text);
		page.setCaretPosition(0);
		page.setModified(false);
		page.clearUndo();
		return page;
	}

	private EditorPage createIfNotOpened(File file)
	{
		EditorPage[] pages = tabView.getPages();
		for (int i = 0; i < pages.length; i++)
		{
			EditorPage page = pages[i];
			if (page.hasFile())
			{
				if (page.getFile().equals(file))
				{
					return page;
				}
			}
		}
		return createEmptyPage();
	}

	private EditorPage createEmptyPage()
	{
		EditorPage page = tabView.createEmptyPage();
		page.addFileDropListener(fileDropListener);
		page.addHighlight(hlFlags);
		page.setShowTabs(Env.is("SHOW_TABS"));
		page.setShowEols(Env.is("SHOW_LINE_DELIMITERS"));
		return page;
	}

	private void editorFileSave(File file) throws IOException
	{
		EditorPage page = tabView.getSelectedPage();
		String encoding = Env.get("EDITOR_FILE_WRITE_ENCODING");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), encoding));
		page.write(writer);
		writer.close();
		page.setFile(file);
		page.setModified(false);
		FrontEnd.println("(EDITOR) file save. [ " + file.getName() + " ]");
	}

	/**
	 * 変更のあるすべてのページについて保存について聞く。
	 * キャンセルが選択された場合はページは1つも閉じられない。
	 * @return すべての確認で「はい」か「いいえ」が選択された場合は {@code true}、ある確認について「キャンセル」が選択された場合は {@code false}
	 */
	public boolean askSaveAllChangedFiles()
	{
		boolean approved = true;
		for (int i = 0; i < tabView.getTabCount(); i++)
		{
			if (isChanged(i))
			{
				tabView.setSelectedPage(i);
				DialogResult ret = askSaveChangedFile();
				if (ret == DialogResult.CANCEL)
				{
					approved = false;
					break;
				}
				else if (ret == DialogResult.YES)
				{
					fileSave();
				}
			}
		}
		return approved;
	}

	/**
	 * すべて閉じる
	 */
	public void closeAllPages()
	{
		if (askSaveAllChangedFiles())
		{
			while (0 < tabView.getTabCount())
			{
				tabView.setSelectedPage(0);
				closeSelectedPageDiscardChanges();
			}
		}
	}

	/**
	 * 選択しているページを閉じる
	 */
	public boolean closeSelectedPage()
	{
		EditorPage page = tabView.getSelectedPage();
		boolean ret = true;
		if (page.isModified())
		{
			String title = page.hasFile() ? page.getFile().getName() : "untitled";
			String message = Env.getMsg(MsgID.text_modified, title);

			AskDialogBuilder builder = AskDialogBuilder.getInstance();
			builder
				.setButtonCaptions(Env.getMsg(MsgID.text_yes), Env.getMsg(MsgID.text_no), Env.getMsg(MsgID.text_cancel))
				.setDialogTitle(title)
				.setText(message)
				.setMessageType(MessageType.QUESTION);
			DialogResult res = builder.showDialog();
			if (res == DialogResult.YES)
			{
				ret = fileSave();
			}
			else if (res == DialogResult.CANCEL)
			{
				ret = false;
			}
		}
		if (ret)
		{
			closeSelectedPageDiscardChanges();
		}
		return ret;
	}

	/**
	 * 選択しているページを閉じる。
	 * このメソッドを直接呼んだ場合、閉じられるページの変更内容は破棄される。
	 */
	public void closeSelectedPageDiscardChanges()
	{
		tabView.closeSelectedPage();
	}

	private File chooseOpenFile()
	{
		String chooserDir = Env.get("EDITOR_FILE_LAST_CHOOSER_DIR");
		if (chooserDir == null || !FileUtils.exists(chooserDir))
		{
			File dirDemo = new File("demo");
			chooserDir = dirDemo.exists() ? dirDemo.getAbsolutePath() : ".";
		}

		JFileChooser jfc = new JFileChooser(chooserDir);
		jfc.addChoosableFileFilter(FileFilters.getLMNFileFilter());
		jfc.addChoosableFileFilter(FileFilters.getILFileFilter());
		jfc.setFileFilter(FileFilters.getLMNFileFilter());
		int r = jfc.showOpenDialog(FrontEnd.mainFrame);
		if (r != JFileChooser.APPROVE_OPTION)
		{
			return null;
		}
		File file = jfc.getSelectedFile();
		Env.set("EDITOR_FILE_LAST_CHOOSER_DIR", jfc.getCurrentDirectory().getAbsolutePath());
		return file;
	}

	private File chooseWriteFile()
	{
		FileFilter lmnFilter = FileFilters.getLMNFileFilter();
		FileFilter ilFilter = FileFilters.getILFileFilter();

		JFileChooser jfc = new JFileChooser(Env.get("EDITOR_FILE_LAST_CHOOSER_DIR"));
		jfc.addChoosableFileFilter(lmnFilter);
		jfc.addChoosableFileFilter(ilFilter);
		jfc.setFileFilter(lmnFilter);

		File file = null;
		while (true)
		{
			int r = jfc.showSaveDialog(FrontEnd.mainFrame);
			if (r != JFileChooser.APPROVE_OPTION)
			{
				return null;
			}
			file = jfc.getSelectedFile();

			if (!file.exists() && !FileUtils.hasExtension(file.getName()))
			{
				if (jfc.getFileFilter() == lmnFilter)
				{
					file = new File(file.getAbsolutePath() + ".lmn");
				}
				else if (jfc.getFileFilter() == ilFilter)
				{
					file = new File(file.getAbsolutePath() + ".il");
				}
			}

			if (file.exists())
			{
				AskDialogBuilder builder = AskDialogBuilder.getInstance();
				builder
					.setButtonCaptions(Env.getMsg(MsgID.text_yes), Env.getMsg(MsgID.text_no), Env.getMsg(MsgID.text_cancel))
					.setDialogTitle(Env.getMsg(MsgID.text_overwrite))
					.setText(Env.getMsg(MsgID.text_already_exists, file.getName()))
					.setMessageType(MessageType.QUESTION);
				DialogResult res = builder.showDialog();
				if (res == DialogResult.YES)
				{
					Env.set("EDITOR_FILE_LAST_CHOOSER_DIR", file.getParent());
					return file;
				}
				else if (res == DialogResult.CANCEL)
				{
					return null;
				}
				else
				{
					// もう一度選択
				}
			}
			else
			{
				Env.set("EDITOR_FILE_LAST_CHOOSER_DIR", file.getParent());
				return file;
			}
		}
	}

	/**
	 * 変更を保存するか聞く。
	 */
	private DialogResult askSaveChangedFile()
	{
		EditorPage page = tabView.getSelectedPage();
		String title = page.hasFile() ? page.getFile().getName() : "untitled";
		String message = Env.getMsg(MsgID.text_modified, title);

		AskDialogBuilder builder = AskDialogBuilder.getInstance();
		builder
			.setButtonCaptions(Env.getMsg(MsgID.text_yes), Env.getMsg(MsgID.text_no), Env.getMsg(MsgID.text_cancel))
			.setDialogTitle(title)
			.setText(message)
			.setMessageType(MessageType.QUESTION);
		return builder.showDialog();
	}

	public void loadFont()
	{
		Font font = Env.getEditorFont();
		tabView.setFont(font);
		setTabWidth(Env.getInt("EDITOR_TAB_SIZE"));
	}

	public boolean isChanged()
	{
		return isChanged(tabView.getSelectedIndex());
	}

	public boolean isChanged(int pageIndex)
	{
		EditorPage[] pages = tabView.getPages();
		if (pageIndex < 0 || pages.length < pageIndex)
		{
			return false;
		}
		EditorPage page = pages[pageIndex];
		return !page.hasFile() || page.isModified();
	}

	public String getFileName()
	{
		if (tabView.getTabCount() == 0)
		{
			return "";
		}

		if (!tabView.getSelectedPage().hasFile())
		{
			return "";
		}
		else
		{
			return tabView.getSelectedPage().getFile().getName();
		}
	}

	private void setTabWidth(int charactersPerTab)
	{
		for (EditorPage page : tabView.getPages())
		{
			page.setTabWidth(charactersPerTab);
		}
	}

	public boolean canUndo()
	{
		if (tabView.getTabCount() > 0)
		{
			return tabView.getSelectedPage().canUndo();
		}
		return false;
	}

	public boolean canRedo()
	{
		if (tabView.getTabCount() > 0)
		{
			return tabView.getSelectedPage().canRedo();
		}
		return false;
	}

	public void editorUndo()
	{
		EditorPage page = tabView.getSelectedPage();
		if (page != null)
		{
			page.undo();
		}
	}

	public void editorRedo()
	{
		EditorPage page = tabView.getSelectedPage();
		if (page != null)
		{
			page.redo();
		}
	}

	public void redoundoUpdate()
	{
		//FrontEnd.mainFrame.mainMenuBar.updateUndoRedo(undoredoManager.canUndo(), undoredoManager.canRedo());
	}

	private static void addFontSize(int addition)
	{
		int fontSize = Env.getInt("EDITOR_FONT_SIZE", 12) + addition;
		if (0 < fontSize && fontSize < 120)
		{
			Env.set("EDITOR_FONT_SIZE", fontSize);
			FrontEnd.loadAllFont();
		}
	}

	private static void scaleDefault()
	{
		Env.set("EDITOR_FONT_SIZE", 12);
		FrontEnd.loadAllFont();
	}

	private static Action getScaleUpAction()
	{
		return new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				addFontSize(1);
			}
		};
	}

	private static Action getScaleDownAction()
	{
		return new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				addFontSize(-1);
			}
		};
	}

	private static Action getScaleDefaultAction()
	{
		return new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				scaleDefault();
			}
		};
	}

	private class FileDropAction implements FileDropListener
	{
		public void filesDropped(List<File> files)
		{
			openFiles(files);
		}
	}
}
