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

package lavit.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FileUtils
{
	private FileUtils() { }

	/**
	 * パス名文字列 {@code path} が表すファイルの存在を確認します。
	 * @param path パス名文字列
	 * @return パス名文字列 {@code path} が表すファイルが存在する場合は {@code true}、そうでない場合は {@code false}。
	 */
	public static boolean exists(String path)
	{
		File file = new File(path);
		return file.exists();
	}

	public static String removeExtension(String fileName)
	{
		int i = fileName.lastIndexOf('.');
		if (i != -1)
		{
			fileName = fileName.substring(0, i);
		}
		return fileName;
	}

	public static String getExtension(String fileName)
	{
		int i = fileName.lastIndexOf('.');
		if (i != -1)
		{
			return fileName.substring(i + 1);
		}
		return "";
	}

	public static boolean hasExtension(String fileName)
	{
		int i = fileName.lastIndexOf('.');
		return 0 < i && i < fileName.length() - 1;
	}

	public static List<File> enumFiles(File dir, FileFilter filter)
	{
		List<File> files = new ArrayList<File>();
		enumFiles(files, dir, filter);
		return files;
	}

	private static void enumFiles(List<File> list, File dir, FileFilter filter)
	{
		List<File> files = new ArrayList<File>();
		if (dir.exists() && dir.isDirectory())
		{
			for (File child : dir.listFiles(filter))
			{
				if (child.isFile())
				{
					files.add(child);
				}
				else if (child.isDirectory())
				{
					list.add(child);
				}
			}
		}
		Collections.sort(list, FileNameComparator.getInstance());
		Collections.sort(files, FileNameComparator.getInstance());
		list.addAll(files);
	}

	private static class FileNameComparator implements Comparator<File>
	{
		private static FileNameComparator instance;

		public int compare(File a, File b)
		{
			return a.getName().compareTo(b.getName());
		}

		public static synchronized FileNameComparator getInstance()
		{
			if (instance == null)
			{
				instance = new FileNameComparator();
			}
			return instance;
		}
	}
}
