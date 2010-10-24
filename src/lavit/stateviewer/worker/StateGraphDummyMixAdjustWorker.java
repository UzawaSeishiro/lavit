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

package lavit.stateviewer.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lavit.Env;
import lavit.Lang;
import lavit.frame.ChildWindowListener;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;

public class StateGraphDummyMixAdjustWorker extends Thread {
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;

	public StateGraphDummyMixAdjustWorker(StateGraphPanel panel){
		this.panel = panel;
		this.drawNodes = panel.getDrawNodes();
	}

	public void run() {
		panel.setActive(false);

		boolean crossreduction_dummyonly = Env.is("SV_CROSSREDUCTION_DUMMYONLY");
		Env.set("SV_CROSSREDUCTION_DUMMYONLY",true);

		drawNodes.removeDummy();

		(new StateGraphAdjustWorker(panel)).waitExecute();

		drawNodes.setBackDummy();
		drawNodes.dummyCentering();
		drawNodes.updateNodeLooks();

		(new StateGraphExchangeWorker(panel)).waitExecute();
		(new StateGraphDummySmoothingWorker(panel)).waitExecute();

		drawNodes.updateNodeLooks();

		Env.set("SV_CROSSREDUCTION_DUMMYONLY", crossreduction_dummyonly);

		panel.autoCentering();
		panel.setActive(true);
	}

}
