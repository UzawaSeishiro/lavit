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
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
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

package lavit.statespacewithscc;

import java.lang.String;

import lavit.FrontEnd;

/* StateViewer-with-SCCボタン用. */
public class StatePanelManager {
    private String statespace;
    private String sccgraph;
    private String[] sccinternalgraph;

    public StatePanelManager(String bufferstring) {
        SlimOutputChopper(bufferstring);
    }

    public void start() {
        ShowStateSpace();
    }

    public void ShowStateSpace() {
        FrontEnd.mainFrame.toolTab.statePanel.start(statespace, false, false);
    }

    public void ShowSCCGraph() {
        FrontEnd.mainFrame.toolTab.statePanel.start(sccgraph, false, false);
    }

    public void ShowSCCInternalGraph(int SCCNodeID) {
        FrontEnd.mainFrame.toolTab.statePanel.start(sccinternalgraph[SCCNodeID], false, false);
    }

    /**
      *   生成時にバッファを通して読み込んだslimの出力を状態空間, SCCグラフ, SCC1内部グラフ, ……, SCCn内部グラフ用に切り分ける.
      */
    private void SlimOutputChopper(String bufferstring) {
        String[] strings = bufferstring.split("StateSpace|SCCGraph|SCCInternalGraph");
        this.statespace = strings[0];
        this.sccgraph = strings[1];
        for (int n = 2; n < strings.length; n++) {
            this.sccinternalgraph[n-2] = strings[n];
        }
    }
}
