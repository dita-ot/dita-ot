/*Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies).
All rights reserved.
*/

/** @file
This is a test file that contains a single class with Doxygen documentation.
This is based on, among others, http://www.stack.nl/~dimitri/doxygen/docblocks.html.
*/

// Global stuff

/** Searches a buffer for a character from a start position in the buffer.
@param aBuffer The buffer to search.
@param c the character to search for.
@param i The start position.
@return The index in the buffer or -1 if not found or i is >= the buffer length.
*/
static int SearchBuffer(const char * aBuffer, char c, int i);

/** A structure that represents a single point. */
typedef struct Point {
	/** The X value. */
	int x;
	/** The Y value. */
	int y;
} PointType;

/** Global structure that defines the origin */
PointType Origin;

/**
 *  A test class.
 */
class TestClass
{
public:
	/** 
	* An enum.
	* More detailed enum description.
	*/
	enum TEnum { 
		TVal1, /**< enum value TVal1. */  
		TVal2, /**< enum value TVal2. */  
		TVal3  /**< enum value TVal3. */  
	} 
	*enumPtr, /**< enum pointer. Details. */
	enumVar;  /**< enum variable. Details. */
	/**
	* The default constructor.
	*/
	TestClass();

	/**
	* A constructor with an integer argument.
	* Have a look at @see TestClass() for the default constructor.
	*/
	TestClass(int aInt);

	/**
	* A destructor. Its virtual because, well, that is good practice
	* when we have virtuals.
	* A more elaborate description of the destructor.
	*/
	virtual ~TestClass();

	/**
	* a normal member taking two arguments and returning an integer value.
	* @param a an integer argument.
	* @param s a constant character pointer.
	* @see Test()
	* @see ~Test()
	* @see testMeToo()
	* @see publicVar()
	* @return The test results
	*/
	int testMe(int a, const char *s);

	/**
	* A pure virtual member.
	* @see testMe()
	* @param c1 the first argument.
	* @param c2 the second argument.
	*/
	virtual void testMeToo(char c1,char c2) = 0;

	/** 
	* A public variable. TODO: Make this protected or private and add
	* a get and set function like @see privateVar and @see PrivateVar()
	*/
	int publicVar;

	/**
	* a function variable.
	* Details.
	*/
	int (*handler)(int a,int b);
private:
	/** 
	* A private variable set by @see PrivateVar(int) and obtained by
	@see PrivateVar() const.
	*/
	int privateVar;
public:
	/**
	* A setting function for privateVar.
	* @param aVal The value to set.
	* @return The value of privateVar
	*/
	int PrivateVar(int aVal);
	/**
	* A getting function for privateVar.
	* @return The value of privateVar
	*/
	int PrivateVar() const;
public:
	/** Nested class declaration
	From: ISO/IEC 14882:2003(E) 9.7 Nested class declarations [class.nest] */
	class inner {
	public:
		/** A public integer. */
		static int x;
		/** Does somenthing. */
		void f(int i);
	};
};



