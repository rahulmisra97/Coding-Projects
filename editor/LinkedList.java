package editor;
import javafx.scene.text.*;
import java.util.*;

public class LinkedList {

	public class Node {
		public Text item;
		public Node next;
		public Node prev;

		public Node (Node p, Text i, Node n) {
			item = i;
			prev = p;
			next = n;
		}
	}

	public Node cursor;
	private int size;
	private Node sentinel;
	public Node ptr;
	public Node spacePtr;

	public LinkedList() {
		size = 0;
		sentinel = new Node(sentinel, new Text(5, 0, ""), sentinel);
		cursor = sentinel;
		ptr = sentinel;
		spacePtr = sentinel;
	}

	public void add(Text x) {
		if (size < 1) {
			Node newLastNode = new Node(sentinel, x, sentinel);
			sentinel.next = newLastNode;
			sentinel.prev = newLastNode;
			cursor = sentinel.next;
			size += 1;
		}
		else {
			Node oldNodeAfter = cursor.next;
			Node oldNodeBefore = cursor;
			Node newNode = new Node(oldNodeBefore, x, oldNodeAfter);
			oldNodeAfter.prev = newNode;
			oldNodeBefore.next = newNode;
			cursor = cursor.next;
			size += 1;
		}
	}

	public Node getSpacePtr() {
		return spacePtr;
	}

	public boolean checkCursorPrevIsSentinel() {
		return cursor.prev == sentinel;
	}

	public void setSpacePtr(Node PLZWORK) {
		spacePtr = PLZWORK;
	}

	public Text getSpacePtrContent() {
		return spacePtr.item;
	}

	public Text getNextSpacePtrContent() {
		return spacePtr.next.item;
	}

	public Text getPrevSpacePtrContent() {
		return spacePtr.prev.item;
	}

	public void moveSpacePtrRight() {
		spacePtr = spacePtr.next;
	}

	public void moveSpacePtrLeft() {
		spacePtr = spacePtr.prev;
	}

	public void resetSpacePtr() {
		spacePtr = sentinel;
	}

	public void movePtrRight() {
		if (size == 0) {
			ptr = sentinel.next;
		}
		else {
			ptr = ptr.next;
		}
	}
	public void setPtr(Node thisProjectIsOuch) {
		ptr = thisProjectIsOuch;
	}

	public Node getPtr() {
		return ptr;
	}

	public Node getSentinel() {
		return sentinel;
	}

	public Text getSentinelPrev() {
		return sentinel.prev.item;
	}

	public boolean checkPtr() {
		return ptr != sentinel;
	}

	public boolean checkSpacePtr() {
		return spacePtr != ptr;
	}

	public Text getPrevCursorText() {
		return cursor.prev.item;
	}

	public void movePtrLeft() {
		ptr = ptr.prev;
	}

	public void resetPtr() {
		ptr = sentinel;
	}

	public void setCursor(Node bruh) {
		cursor = bruh;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public Text getPtrContent() {
		return ptr.item;
	}

	public Text getPrevPtrContent() {
		return ptr.prev.item;
	}

	public Text getNextPtrContent() {
		return ptr.next.item;
	}

	public void setSpacePtrToPtr() {
		spacePtr = ptr;
	}

	public void setPtrToSpacePtr() {
		ptr = spacePtr;
	}

	public int size() {
		return size;
	}

	public Text remove() {
		if (size == 0) {
			return null;
		}
		else if (size == 1) {
			Node removedLast = sentinel.prev;
			sentinel.prev = sentinel;
			sentinel.next = sentinel;
			size -= 1;
			cursor = sentinel;
			return removedLast.item;
		}
		else {
			Node removed = cursor;
			Node oldCursorNext = cursor.next;
			cursor = cursor.prev;
			oldCursorNext.prev = cursor;
			cursor.next = oldCursorNext;
			size -= 1;
			return removed.item;
		}
	}

	public Text getContent(int index) {
		Node a = sentinel.next;
		if (size <= index) {
			return null;
		}
		else if (index <= 0) {
			return sentinel.next.item;
		}
		else {
			int i = 0;
			while (i < index) {
				a = a.next;
				i += 1;
			}
			return a.item;
		}
	}

	public Text getPreviousText() {
		if (size == 0) {
			return null;
		}
		else {
			return cursor.prev.item;
		}
	}

	public Text getCursorText() {
		if (cursor.equals(sentinel)) {
			return sentinel.item;
		}
		return cursor.item;
	}

	public Node getCursor() {
		return cursor;
	}

	public void cursorLeftKey() {
		if (cursor.equals(sentinel)) {
			return;
		}
		cursor = cursor.prev;
	}

	public void cursorRightKey() {
		if (cursor.next.equals(sentinel)) {
			return;
		}
		cursor = cursor.next;
	}

	public void cursorUpKey() {
		Node cursorCopy = cursor;
		int cursorCopyX = Math.round((int)cursorCopy.item.getX() + Math.round((int)cursorCopy.item.getLayoutBounds().getWidth()));
		int cursorCopyY = Math.round((int)cursorCopy.item.getY());
		List<Double> list = new ArrayList();
		if (cursorCopyX == 5) {
			while (cursorCopy.prev.prev.item.getX() + Math.round((int) cursorCopy.prev.prev.item.getLayoutBounds().getWidth()) != 5) {
				cursorCopy = cursorCopy.prev;
			}
			cursor = cursorCopy.prev.prev;
		}
		else {
			while (cursorCopy.prev.item.getY() == cursorCopyY) {
				cursorCopy = cursorCopy.prev;
			}
			cursorCopy = cursorCopy.prev;
			Node cursorCopyCorrectLine = cursorCopy;
			while (Math.round(cursorCopy.item.getX() + cursorCopy.item.getLayoutBounds().getWidth()) != 5) {
				list.add(cursorCopy.item.getX() + Math.round((int)cursorCopy.item.getLayoutBounds().getWidth()));
				cursorCopy = cursorCopy.prev;
			}
			double min = Math.abs(list.get(0) - cursorCopyX);
			for(int i = 0; i < list.size(); i++) {
				double number = Math.abs(list.get(i) - cursorCopyX);
				if (number < min) min = number;
			}
			while (Math.abs(cursorCopyCorrectLine.item.getX() + Math.round((int)cursorCopyCorrectLine.item.getLayoutBounds().getWidth()) - cursorCopyX) != min) {
				cursorCopyCorrectLine = cursorCopyCorrectLine.prev;
			}
			cursor = cursorCopyCorrectLine.prev;
		}
	}

	public void cursorDownKey() {
		Node cursorCopy = cursor;
		double cursorCopyX;
		double cursorCopyY;
		if (cursorCopy == sentinel) {
			cursorCopyX = 5;
			cursorCopyY = 0;
		}
		else {
			cursorCopyX = cursorCopy.item.getX() + Math.round(getCursorText().getLayoutBounds().getWidth());
			cursorCopyY = cursorCopy.item.getY();
		}
		List<Double> list = new ArrayList();
		if (cursorCopyX == 5 || cursorCopyX == 6) {
			while (cursorCopy.next.item.getY() == cursorCopyY) {
				cursorCopy = cursorCopy.next;
			}
			while (cursorCopy.next.item.getX() != 5) {
				cursorCopy = cursorCopy.next;
			}
			cursor = cursorCopy.next;
		}
		else {
			while (cursorCopy.next.item.getY() == cursorCopyY) {
				cursorCopy = cursorCopy.next;
			}
			cursorCopy = cursorCopy.next;
			Node cursorCopyCorrectLine = cursorCopy;
			while (!cursorCopy.equals(sentinel)) {
				list.add(cursorCopy.item.getX() + Math.round(getCursorText().getLayoutBounds().getWidth()));
				cursorCopy = cursorCopy.next;
			}
			double min = Math.abs(list.get(0) - cursorCopyX);
			for(int i = 0; i < list.size(); i++) {
				double number = Math.abs(list.get(i) - cursorCopyX);
				if (number < min) min = number;
			}
			while (Math.abs(cursorCopyCorrectLine.next.item.getX() + Math.round(getCursorText().getLayoutBounds().getWidth()) - cursorCopyX) != min) {
				cursorCopyCorrectLine = cursorCopyCorrectLine.next;
			}
			cursor = cursorCopyCorrectLine;
		}
	}


	public Node getNode(int index) {
		Node a = sentinel.next;
		if (size <= index) {
			return null;
		}
		else {
			int i = 0;
			while (i < index) {
				a = a.next;
				i+=1;
			}
			return a;
		}
	}

}