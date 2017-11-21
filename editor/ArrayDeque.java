public class ArrayDeque<Item> {

	private Item[] items;
	private int size;
	
	private int front;
	private int back;
	

	public ArrayDeque() {
		size = 0;
		front = 0;
		back = 0;
		items = (Item[]) new Object[8];
	}

	private void resize(int capacity) {
		Item[] a = (Item[]) new Object[capacity];
		for(int i = 0; i<items.length; i++) {
			a[i] = get(i);
		}

	}

	private void checkResize() {
        if (size * 1.0 / items.length < 0.25 && items.length > 16) {
            resize(items.length / 2);
        }
        if (size == items.length) {
            resize(items.length * 2);
        }
    }
	
	private void addFirstIndexModifier(int x) {
		if (front == 0) {
			front = items.length - 1;
		}
		else {
			front -= 1;
		}
	}

	private void addLastIndexModifier(int x) {
		if (back == items.length - 1) {
			back = 0;
		}
		else {
			back += 1;
		}
	}
	
	public void addFirst(Item x) {
			addFirstIndexModifier(front);
			size += 1;
			if (size == 1) {
				items[back] = x;
			}
			else {
				items[front + 1] = x;
			}
	}

	public void addLast(Item x) {
		size += 1;
		if (size == 1) {
			items[back] = x;
			addFirstIndexModifier(front);
		}
		else {
			if (back == items.length - 1) {
				items[0] = x;
				addLastIndexModifier(back);
			}
			else {
				items[back + 1] = x;
				addLastIndexModifier(back);
			}
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}
	
	public void printDeque() {
		int copyFront = front;
		while (copyFront < items.length - 1) {
			if (items[copyFront+1] != null){
				System.out.print(items[copyFront+1] + " ");
			}
			copyFront += 1;
		}
		int i = 0;
		while (i <= back) {
			if (items[i] != null) {
				System.out.print(items[i] + " ");
			}
			i += 1;
		}
	}

	private void removeFirstIndexModifier(int x) {
		if (front == items.length - 1) {
			front = 0;
		}
		else {
			front += 1;
		}
	}

	private Item removedFirstItem() {
		if (front == items.length - 1) {
			Item removedFront = items[0];
			return removedFront;
		}
		else {
			Item removedFront = items[front + 1];
			return removedFront;
		}
	} 
	public Item removeFirst() {
		if (size == 0){
			return null;
		}
		else if (size == 1){
			size -= 1;
			Item removedVal = removedFirstItem();

			items[front] = null;
			removeFirstIndexModifier(front);
			return removedVal;
		}
		else {
			size -= 1;
			Item removedVal = removedFirstItem();
			if (front == items.length - 1) {
				front = 0;
				items[front] = null;
				return removedVal;
			}
			else {
				items[front + 1] = null;
				removeFirstIndexModifier(front);
				return removedVal;
			}
		}
	}

	private void removeLastIndexModifier(int x) {
		if (back == 0) {
			back = items.length - 1;
		}
		else {
			back -= 1;
		}
	}


	public Item removeLast() {
		Item removedLast = items[back];
		if (size == 0) {
			return null;
		} 
		else if (size == 1) {
			size -= 1;
			items[back] = null;
			removeFirstIndexModifier(front);
			return removedLast;
		}
		else {
			size -= 1;
			items[back] = null;
			removeLastIndexModifier(back);
			return removedLast;
		}
	}

	public Item get(int index) {
		int ctr = 0;
		for (int i = front + 1; i < items.length; i++) {
			if (ctr == index)
				return (Item) items[i];
			ctr += 1;
		}
		for (int i = 0; i < items.length; i++) {
			if (ctr == index) 
				return (Item) items[i];
			ctr++;
	}
	return null;}
}