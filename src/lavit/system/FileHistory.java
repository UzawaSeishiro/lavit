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

package lavit.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lavit.Env;

/**
 * <p>開いたファイルの履歴を管理します。</p>
 * @author Yuuki SHINOBU
 */
public class FileHistory
{
	/**
	 * <p>ファイル履歴を保持する数の上限値の既定値です。</p>
	 */
	public static final int DEFAULT_LIMIT = 8;

	private static final String HISTORY_FILE = "recentfiles";
	private static FileHistory instance;

	private int limit = DEFAULT_LIMIT;
	private LinkedList<File> files = new LinkedList<File>();

	/**
	 * <p>このファイル履歴オブジェクトに設定されているファイルリストの要素数の上限値を取得します。</p>
	 * @return 設定されている上限値
	 */
	public int getLimit()
	{
		return limit;
	}

	/**
	 * <p>このファイル履歴オブジェクトが含むファイルリストの要素数の上限値を設定します。</p>
	 * @param limit 設定する上限値
	 */
	public void setLimit(int limit)
	{
		this.limit = limit;
		trimSize();
	}

	/**
	 * <p>ファイル履歴をすべて削除します。</p>
	 */
	public void clear()
	{
		files.clear();
	}

	/**
	 * <p>ファイルを履歴に追加します。</p>
	 * <p>このファイルが既に含まれる場合、その要素を先頭へ移動します。</p>
	 * @param file 追加するファイル
	 */
	public void add(File file)
	{
		try
		{
			file = file.getCanonicalFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		int index = files.indexOf(file);

		if (index != -1)
		{
			files.remove(index);
		}

		files.addFirst(file);
		trimSize();
	}

	/**
	 * <p>ファイル履歴のリストを読み取り専用として取得します。</p>
	 * @return ファイル履歴のリスト
	 */
	public List<File> getFiles()
	{
		return Collections.unmodifiableList(files);
	}

	/**
	 * <p>ファイル履歴のリストをファイルに保存します。</p>
	 */
	private void save()
	{
		File historyFile = Env.getPropertyFile(HISTORY_FILE);
		String charset = "UTF-8";
		try
		{
			PrintWriter writer = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(historyFile), charset)));
			for (File file : files)
			{
				writer.println(file.getAbsolutePath());
			}
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * <p>ファイルからファイル履歴のリストを読み込みます。</p>
	 * @return ファイル履歴オブジェクト
	 */
	public static FileHistory get()
	{
		if (instance != null)
		{
			return instance;
		}

		instance = new FileHistory();
		File historyFile = Env.getPropertyFile(HISTORY_FILE);
		if (historyFile.exists())
		{
			String charset = "UTF-8";
			try
			{
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(historyFile), charset));
				try
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						instance.add(new File(line));
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					reader.close();
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				instance.save();
			}
		});
		return instance;
	}

	/**
	 * <p>リストの要素数を設定された上限値以下に切り詰めます。</p>
	 */
	private void trimSize()
	{
		if (files.size() > limit)
		{
			if (files.size() == limit + 1)
			{
				files.remove(files.size() - 1);
			}
			else
			{
				files = (LinkedList<File>)files.subList(0, limit - 1);
			}
		}
	}
}
