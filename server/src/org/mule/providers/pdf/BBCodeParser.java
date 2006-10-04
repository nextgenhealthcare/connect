package org.mule.providers.pdf;

public class BBCodeParser {
	private String currentSegment = new String();
	private int currentIndex;
	private String[] startKeywords = { "", "[i]", "[u]", "[b]" };
	private String[] endKeywords = { "", "[/i]", "[/u]", "[/b]" };
	private String[] keywordType = { "NORMAL", "ITALIC", "UNDERLINE", "BOLD" };

	public BBCodeParser(String current) {
		this.currentIndex = 0;
		this.currentSegment = current;
	}

	/*
	 * Get the next token.
	 */
	public BBCodeToken getNext() {
		String result = new String();
		int type = -1;

		while (hasNext()) {
			if (currentSegment.charAt(currentIndex) == '[') {
				for (int i = 1; i < startKeywords.length; i++) {
					if (currentSegment.substring(currentIndex, currentIndex + startKeywords[i].length()).equalsIgnoreCase(startKeywords[i])) {
						if (type == 0)
							return new BBCodeToken(result, keywordType[type]);
						else {
							type = i;
							currentIndex += startKeywords[type].length();
						}
					}
				}

				if (type > 0) {
					for (int i = 1; i < endKeywords.length; i++) {
						if (currentSegment.substring(currentIndex, currentIndex + endKeywords[i].length()).equalsIgnoreCase(endKeywords[i])) {
							if (type == i) {
								currentIndex += endKeywords[i].length();
								return new BBCodeToken(result, keywordType[type]);
							}
						}
					}
				}
			} else if (type == -1)
				type = 0;

			result += currentSegment.charAt(currentIndex);
			currentIndex++;
		}

		return new BBCodeToken(result, keywordType[type]);
	}

	/*
	 * See if there are more tokens.
	 */
	public boolean hasNext() {
		return currentIndex < (currentSegment.length());
	}

	/*
	 * Example for testing
	 */
	public static void main(String[] args) {
		BBCodeParser p = new BBCodeParser("Hello [b]there[/b], how are [i]you[/i]?");
		while (p.hasNext()) {
			BBCodeToken t = p.getNext();
			System.out.println(t.getValue() + " " + t.getType());
		}

		p = new BBCodeParser("[/i][/B]Hello [/i][B]there[/b][b][/b], how are [I]you[/i]?");
		while (p.hasNext()) {
			BBCodeToken t = p.getNext();
			System.out.println(t.getValue() + " " + t.getType());
		}
	}
}
