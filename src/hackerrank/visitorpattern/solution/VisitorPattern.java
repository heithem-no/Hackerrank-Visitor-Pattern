package hackerrank.visitorpattern.solution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VisitorPattern {

	static Map<Integer, Set<Integer>> edges;
	static int[] arrayOfValues;
	static int[] arrayOfColors;
	static Map<Integer, Tree> processedNodes = new HashMap<>();
	static Set<Tree> visited = new HashSet<>();

	interface TreeVis {
		void visitNode(TreeNode node);

		void visitLeaf(TreeLeaf leaf);

		Integer getResult();
	}

	static class SumInLeavesVisitor implements TreeVis {

		private Integer result = 0;

		@Override
		public void visitNode(TreeNode node) {
			// Do nothing
		}

		@Override
		public void visitLeaf(TreeLeaf leaf) {
			result += leaf.getValue();

		}

		@Override
		public Integer getResult() {
			return result;
		}

	}

	static class ProductRedNodesVisitor implements TreeVis {

		private long result = 1;
		static final long modulo = (int) Math.pow(10, 9) + 7;

		@Override
		public void visitNode(TreeNode node) {
			if (node.getColor() == 0) {
				result = (result * node.getValue()) % modulo;
			}
		}

		@Override
		public void visitLeaf(TreeLeaf leaf) {
			if (leaf.getColor() == 0) {
				result = (result * leaf.getValue()) % modulo;
			}
		}

		@Override
		public Integer getResult() {
			return (int) result;
		}

	}

	static class FancyVisitor implements TreeVis {

		private Integer sumTreeNodeEvenDepth = 0;
		private Integer sumGreenLeaves = 0;

		@Override
		public void visitNode(TreeNode node) {
			if (node.getDepth() % 2 == 0) {
				sumTreeNodeEvenDepth += node.getValue();
			}
		}

		@Override
		public void visitLeaf(TreeLeaf leaf) {
			if (leaf.getColor() == 1) {
				sumGreenLeaves += leaf.getValue();
			}
		}

		@Override
		public Integer getResult() {
			return Math.abs(sumTreeNodeEvenDepth - sumGreenLeaves);
		}
	}

	abstract static class Tree {
		private int value;
		private int color;
		private int depth;

		public Tree(int value, int color, int depth) {
			this.value = value;
			this.color = color;
			this.depth = depth;
		}

		public int getValue() {
			return value;
		}

		public int getColor() {
			return color;
		}

		public int getDepth() {
			return depth;
		}

		public abstract void accept(TreeVis visitor);
	}

	static class TreeNode extends Tree {
		private Set<Tree> children = new HashSet<>();

		public Set<Tree> getChildren() {
			return children;
		}

		public TreeNode(int value, int color, int depth) {
			super(value, color, depth);
		}

		@Override
		public void accept(TreeVis visitor) {
			visitor.visitNode(this);
		}

		public void addChild(final Tree node) {
			children.add(node);
		}
	}

	static class TreeLeaf extends Tree {

		public TreeLeaf(int value, int color, int depth) {
			super(value, color, depth);
		}

		public void accept(TreeVis visitor) {
			visitor.visitLeaf(this);
		}
	}

	private static Tree buildTree() {
		try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
			final int nodesNumber = Integer.parseInt(bufferedReader.readLine().trim());
			arrayOfValues = Stream.of(bufferedReader.readLine().replaceAll("\\s+$", "").split(" "))
					.mapToInt(Integer::parseInt).toArray();
			arrayOfColors = Stream.of(bufferedReader.readLine().replaceAll("\\s+$", "").split(" "))
					.mapToInt(Integer::parseInt).toArray();

			if (nodesNumber == 1) {
				return new TreeLeaf(arrayOfValues[0], arrayOfColors[0], 0);
			}

			edges = new HashMap<>();

			for (int i = 1; i <= nodesNumber - 1; i++) {
				final List<Integer> edge = Stream.of(bufferedReader.readLine().replaceAll("\\s+$", "").split(" "))
						.map(Integer::parseInt).collect(Collectors.toList());
				Set<Integer> childrenOfVertex0 = edges.get(edge.get(0));
				if (null == childrenOfVertex0) {
					childrenOfVertex0 = new HashSet<>();
				}
				childrenOfVertex0.add(edge.get(1));
				edges.put(edge.get(0), childrenOfVertex0);

				Set<Integer> childrenOfVertex1 = edges.get(edge.get(1));
				if (null == childrenOfVertex1) {
					childrenOfVertex1 = new HashSet<>();
				}
				childrenOfVertex1.add(edge.get(0));
				edges.put(edge.get(1), childrenOfVertex1);
			}

			final TreeNode root = new TreeNode(arrayOfValues[0], arrayOfColors[0], 0);
			addChildren(root, 1);

			return root;
		} catch (IOException e) {
			return null;
		}

	}

	static void addChildren(final TreeNode node, final Integer nodeIndex) {
		if (processedNodes.containsKey(nodeIndex)) {
			return;
		}
		processedNodes.put(nodeIndex, node);
		final Set<Integer> childrenIndexes = edges.get(nodeIndex);
		if (null != childrenIndexes && !childrenIndexes.isEmpty()) {
			for (Integer childIndex : childrenIndexes) {
				Tree childNode = processedNodes.get(childIndex);
				if (null == childNode) {
					if (null == edges.get(childIndex) || edges.get(childIndex).size() == 1) {
						childNode = new TreeLeaf(arrayOfValues[childIndex - 1], arrayOfColors[childIndex - 1],
								node.getDepth() + 1);
					} else {
						childNode = new TreeNode(arrayOfValues[childIndex - 1], arrayOfColors[childIndex - 1],
								node.getDepth() + 1);
						addChildren((TreeNode) childNode, childIndex);
					}
					node.addChild(childNode);
				}
			}
		}
	}

	public static void acceptVisitors(final Tree tree, final TreeVis sumInLeavesVisitor, final TreeVis productVisitor,
			final TreeVis fancyVisitor) {
		if (!visited.contains(tree)) {
			tree.accept(sumInLeavesVisitor);
			tree.accept(productVisitor);
			tree.accept(fancyVisitor);
		}
		if (tree instanceof TreeNode) {
			for (Tree child : ((TreeNode) tree).getChildren()) {
				if (!visited.contains(child)) {
					acceptVisitors(child, sumInLeavesVisitor, productVisitor, fancyVisitor);
				}
			}
		}

	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		final TreeVis sumInLeavesVisitor = new SumInLeavesVisitor();
		final TreeVis productVisitor = new ProductRedNodesVisitor();
		final TreeVis fancyVisitor = new FancyVisitor();

		Tree root = buildTree();

		acceptVisitors(root, sumInLeavesVisitor, productVisitor, fancyVisitor);

		System.out.println(sumInLeavesVisitor.getResult());
		System.out.println(productVisitor.getResult());
		System.out.println(fancyVisitor.getResult());

	}

}
