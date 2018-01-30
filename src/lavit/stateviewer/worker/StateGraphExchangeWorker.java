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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lavit.Env;
import lavit.StopWatch;
import lavit.frame.ChildWindowListener;
import lavit.localizedtext.MsgID;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;

public class StateGraphExchangeWorker extends SwingWorker<Object,Object>{
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;
	private boolean endFlag;
	private boolean changeActive;

	private ProgressFrame frame;

	public StateGraphExchangeWorker(StateGraphPanel panel){
		this.panel = panel;
		this.drawNodes = panel.getDrawNodes();
		this.endFlag = false;
		this.changeActive = true;
	}

	public void waitExecute(){
		this.changeActive = false;
		selectExecute();
		while(!endFlag){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}

	public void selectExecute(){
		if(drawNodes.size()<1000){
			atomic();
		}else{
			ready();
			execute();
		}
	}

	public void atomic(){
		ready(false);
		doInBackground();
		done();
	}

	public void ready(){
		ready(true);
	}

	public void ready(boolean open){
		if(changeActive) panel.setActive(false);
		if(open){
			frame = new ProgressFrame();
			addPropertyChangeListener(frame);
		}
	}

	public void end() {
		panel.autoCentering();
		if(changeActive) panel.setActive(true);
		if(frame!=null) frame.dispose();
		this.endFlag = true;
	}

	@Override
	protected Object doInBackground(){

		StopWatch.startWatch("Exchange[1]");

		if(Env.is("SV_CROSSREDUCTION_DUMMYONLY")&&drawNodes.getDummySize()==0){ end(); return null; }

		int originalNum = getAllCross();
		StatePositionSet original = new StatePositionSet(drawNodes);

		boolean res;
		List<List<StateNode>> depthNode = drawNodes.getDepthNode();

		//前から後ろへ
		res = reduction(1, depthNode.size(), -1, 0, depthNode.get(0).size());
		if(!res){ end(); return null; }
		int rightNum = getAllCross();
		StatePositionSet right = new StatePositionSet(drawNodes);

		//後ろから前へ
		res = reduction(depthNode.size()-2, -1, 1, 50, depthNode.get(depthNode.size()-1).size());
		if(!res){ end(); return null; }
		int leftNum = getAllCross();

		if(originalNum<Math.min(rightNum, leftNum)){
			//最初の方が良い場合は戻す
			drawNodes.updatePosition(original);
		}else if(rightNum<leftNum){
			//前から後ろへの方が良い場合は戻す
			drawNodes.updatePosition(right);
		}
		clearAllLayerYs();

		if(frame!=null) frame.end();
		end();

		StopWatch.stopWatch("Exchange[1]");
		return null;
	}


	private boolean reduction(int start, int end, int cmp, int startProgress, int endNodeNum){

		List<List<StateNode>> depthNode = drawNodes.getDepthNode();
		boolean crossReductionDummyOnly = Env.is("SV_CROSSREDUCTION_DUMMYONLY");

		for(int i=start;i!=end;i-=cmp){
			List<StateNode> nodes = depthNode.get(i);
			int backCross = Integer.MAX_VALUE;
			while(true){
				for(StateNode node : nodes){
					node.makeLayerNodeList();
				}
				for(StateNode n1 : nodes){
					if(crossReductionDummyOnly&&!n1.dummy){ continue; }
					for(StateNode n2 : nodes){
						if(crossReductionDummyOnly&&!n2.dummy){ continue; }
						if(n1.getY()<n2.getY()){
							int l=0;
							int c1 = 0;
							int c2 = 0;
							Double[] n1Ys = n1.getLayerYs(cmp);
							Double[] n2Ys = n2.getLayerYs(cmp);
							for(int n=0;n<n1Ys.length;++n){
								c1 += l;
								for(int m=l;m<n2Ys.length;++m){
									if(n1Ys[n]>n2Ys[m]){
										c1++;
									}else if(n1Ys[n]<n2Ys[m]){
										c2+=(n2Ys.length-m);
										l = m;
										break;
									}
								}
							}
							if(c1>c2) swapY(n1,n2);
						}
					}
					if(isCancelled()){ return false; }
				}
				int cross = getLayerCrossNum(i, cmp);
				if(cross>=backCross){ break; }
				backCross = cross;
				if(isCancelled()){ return false; }
			}

			//progress更新
			endNodeNum += nodes.size();
			setProgress(startProgress+50*endNodeNum/drawNodes.size());
			if(isCancelled()){ return false; }
		}
		return true;
	}


/*
	private boolean reduction(int start, int end, int cmp, int startProgress, int endNodeNum){

		ArrayList<ArrayList<StateNode>> depthNode = drawNodes.getDepthNode();

		for(int i=start;i!=end;i-=cmp){
			ArrayList<StateNode> nodes = depthNode.get(i);
			int backCross = Integer.MAX_VALUE;
			while(true){
				for(StateNode n1 : nodes){
					for(StateNode n2 : nodes){
						if(n1.getY()<n2.getY()&&(!Env.is("SV_CROSSREDUCTION_DUMMYONLY")||n1.dummy&&n2.dummy)){
							int c1 = 0;
							int c2 = 0;
							for(StateNode nn1 : getLayerFlowNodes(n1,i+cmp)){
								for(StateNode nn2 : getLayerFlowNodes(n2,i+cmp)){
									if(nn2.getY()<nn1.getY()){
										c1++;
									}else if(nn1.getY()<nn2.getY()){
										c2++;
									}
								}
							}
							if(c1>c2) swapY(n1,n2);
						}
					}
					if(isCancelled()){ return false; }
				}
				int cross = getLayerCross(i+cmp,i);
				if(cross>=backCross){ break; }
				backCross = cross;
				if(isCancelled()){ return false; }
			}

			//progress更新
			endNodeNum += nodes.size();
			setProgress(startProgress+50*endNodeNum/drawNodes.size());
			if(isCancelled()){ return false; }
		}
		return true;
	}
*/

	private void swapY(StateNode n1,StateNode n2){
		double n1y = n1.getY();
		double n2y = n2.getY();
		n1.setPosition(n1.getX(),n2y);
		n2.setPosition(n2.getX(),n1y);
	}

	private ArrayList<StateNode> getLayerFlowNodes(StateNode node,int layer){
		ArrayList<StateNode> backs = new ArrayList<StateNode>();
		for(StateNode from : node.getFromNodes()){
			if(from.depth==layer){
				backs.add(from);
			}
		}
		for(StateNode to : node.getToNodes()){
			if(to.depth==layer){
				if(!backs.contains(to)){ backs.add(to); }
			}
		}
		return backs;
	}

	public int getAllCross(){
		int depth = drawNodes.getDepthNode().size();
		makeAllLayerYs();
		int cross = 0;
		for(int i=0;i<depth;++i){
			cross += getNextLayerCross(i);
			cross += getOneLayerCross(i);
		}
		clearAllLayerYs();
		return cross;
	}

	// TODO 高速化
	private int getLayerCross(int layer1,int layer2){
		List<StateNode> nodes = drawNodes.getDepthNode().get(layer1);
		int cross = 0;
		for(StateNode n1 : nodes){
			for(StateNode n1f : n1.getLayerFlowNodes(layer2)){
				for(StateNode n2 : nodes){
					if(n1.getY()<n2.getY()){
						for(StateNode n2f : n2.getLayerFlowNodes(layer2)){
							if(n2f.getY()<n1f.getY()){
								cross++;
							}
						}
					}
				}
			}
		}
		return cross;
	}

	private void makeAllLayerYs(){
		for(StateNode node : drawNodes.getAllNode()){
			node.makeLayerNodeList();
		}
	}

	private void clearAllLayerYs(){
		for(StateNode node : drawNodes.getAllNode()){
			node.clearLayerNodeList();
		}
	}

	private int getLayerCrossNum(int layer, int cmp){
		if(cmp==-1){
			return getBackLayerCross(layer);
		}else if(cmp==1){
			return getNextLayerCross(layer);
		}else{
			return 0;
		}
	}

	private int getNextLayerCross(int layer){
		int cross = 0;

		//深さを調べる
		List<List<StateNode>> depthNode = drawNodes.getDepthNode();
		if(layer>=depthNode.size()){ return 0; }

		//新しく配列を作成してソート
		List<StateNode> nodes = new ArrayList<StateNode>(depthNode.get(layer));
		Collections.sort(nodes, new Comparator<StateNode>() {
			public int compare(StateNode n1, StateNode n2) {
				if(n1.getY()<n2.getY()){
					return -1;
				}else if(n1.getY()>n2.getY()){
					return 1;
				}else{
					return 0;
				}
			}
		});

		for(int i=0;i<nodes.size();++i){
			for(int j=i+1;j<nodes.size();++j){
				int l=0;
				Double[] n1Ys = nodes.get(i).getNextLayerYs();
				Double[] n2Ys = nodes.get(j).getNextLayerYs();
				for(int n=0;n<n1Ys.length;++n){
					cross += l;
					for(int m=l;m<n2Ys.length;++m){
						if(n1Ys[n]>n2Ys[m]){
							cross++;
						}else{
							l = m;
							break;
						}
					}
				}
			}
		}

		return cross;
	}

	private int getBackLayerCross(int layer){
		int cross = 0;

		//深さを調べる
		List<List<StateNode>> depthNode = drawNodes.getDepthNode();
		if(layer>=depthNode.size()){ return 0; }

		//新しく配列を作成してソート
		List<StateNode> nodes = new ArrayList<StateNode>(depthNode.get(layer));
		Collections.sort(nodes, new Comparator<StateNode>() {
			public int compare(StateNode n1, StateNode n2) {
				if(n1.getY()<n2.getY()){
					return -1;
				}else if(n1.getY()>n2.getY()){
					return 1;
				}else{
					return 0;
				}
			}
		});

		for(int i=0;i<nodes.size();++i){
			for(int j=i+1;j<nodes.size();++j){
				int l=0;
				Double[] n1Ys = nodes.get(i).getBackLayerYs();
				Double[] n2Ys = nodes.get(j).getBackLayerYs();
				for(int n=0;n<n1Ys.length;++n){
					cross += l;
					for(int m=l;m<n2Ys.length;++m){
						if(n1Ys[n]>n2Ys[m]){
							cross++;
						}else{
							l = m;
							break;
						}
					}
				}
			}
		}

		return cross;
	}


	private int getOneLayerCross(int layer){
		List<StateNode> nodes = drawNodes.getDepthNode().get(layer);
		int cross = 0;
		for(StateNode n : nodes){
			for(StateNode to : n.getToNodes()){
				if(n.depth==to.depth&&n.id!=to.id){
					double b,t;
					if(n.getY()<to.getY()){
						b = n.getY();
						t = to.getY();
					}else{
						b = to.getY();
						t = n.getY();
					}
					for(StateNode m : nodes){
						if(b<m.getY()&&m.getY()<t){
							cross++;
						}
					}
				}
			}
		}
		return cross;
	}

	private class ProgressFrame extends JDialog implements PropertyChangeListener,ActionListener {
		private JPanel panel;
		private JProgressBar bar;
		private JButton cancel;

		private ProgressFrame(){
			panel = new JPanel();

			bar = new JProgressBar(0,100);
			bar.setStringPainted(true);
			panel.add(bar);

			cancel = new JButton(Env.getMsg(MsgID.text_cancel));
			cancel.addActionListener(this);
			panel.add(cancel);

			add(panel);

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Adjacent Exchange");
			setIconImages(Env.getApplicationIcons());
			setAlwaysOnTop(true);
			setResizable(false);

	        pack();
	        setLocationRelativeTo(panel);
	        addWindowListener(new ChildWindowListener(this));
	        setVisible(true);
		}

		public void end(){
			bar.setValue(100);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress".equals(evt.getPropertyName())) {
				bar.setValue((Integer)evt.getNewValue());
			}
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==cancel){
				if(!isDone()){
					cancel(false);
				}
			}
		}
	}


}
