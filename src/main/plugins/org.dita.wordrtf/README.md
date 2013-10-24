# DITA to RTF Transform

## Guiding Principles

Improving the RTF output of DITA content means basically the following:

1. From the XSLT perspective: Streamlining the XSLT code which seems to be overly complex (at least in some parts).
2. From the DITA perspective: Reducing the generated RTF code to the minimum necessary to render the content appropriate to the semantic structure of the DITA files.
3. From the RTF perspective: For maximum backward and cross-platform compatibility only a necessary subset of mature RTF features should be used, i.e. those also available in older versions of the RTF standard (the RTF perspective).
4. To allow for easy post-editing, formatting of semantically distinct elements should be provided using RTF styles.

## RTF Basics

### General Information
- RTF is a text based format.
- RTF files are ANSI encoded and can usually only contain ASCII characters directly. All other characters (e.g. all Unicode characters) have to be escaped.
- RTF is mainly a format to be produced automatically. Usually the RTF code is not meant to be manipulated manually. 
- Whereas DITA is all about structuring content semantically, RTF is mostly concerned with formatting. For example, there isn't even a concept of "heading" in RTF. A heading is just a paragraph with a different and larger font in bold.

### Basic Syntax
RTF breaks down into four basic categories: 

1. commands
2. escapes 
3. groups 
4. plaintext

#### 1. Commands
RTF commands consist of a backslash, some lowercase letters, maybe an integer, and then maybe a meaningless space character. Examples: `\pard`, `\fs24`. Commands take effect until the next closing curly bracket.

#### 2. Escapes
Examples: `\~`(nonbreaking space),  `\uc1\u26412*` (Unicode character 本)

#### 3. Groups
Groups are enclosed in `{`curly brackets`}`. They prevent formatting to spill over to the next section so the most maintainable way to enclose a paragraph in RTF is `{\pard ... \par}`.

#### 4. Plaintext
All other characters will be treated as plaintext by RTF, usually also all space characters (but not line breaks).

### Sample RTF File
Here's the barest of a valid RTF file:

```
{\rtf
{\fonttbl 
{\f0 Times New Roman;}
}
\f0\fs60 
Hello, World!
} 
```

## RTF Resources

### RTF Specification
- The most current versions of the RTF specification (1.8 and 1.9.1) are available from [Microsoft](http://search.microsoft.com/en-us/DownloadResults.aspx?q=rtf+specification&sortby=-availabledate)
- Older versions (1.3, 1.5, 1.7) can be found on the [RTF Tools website](http://www.snake.net/software/RTF/).

### Sean M. Burke
The definitive authority regarding RTF is Sean M. Burke. 
- The best reference is his ["RTF Pocket Guide"](http://shop.oreilly.com/product/9780596004750.do) from O'Reilly.  
- Basic information on RTF is also available from his [RTF Cookbook - RTF overview and quick reference](http://search.cpan.org/~sburke/RTF-Writer/lib/RTF/Cookbook.pod) (part of the RTF-Writer Perl module).
- Additional information can be found on his website http://interglacial.com/rtf/.

## Still to be done
- Provide separate RTF styles for all DITA elements which might be rendered distinctly. These styles can later easily be changed with any RTF capable word processor without touching the RTF code.
- More robust whitespace handling. RTF ignores new lines but generally not spaces.
- All features not covered by the hierarchy sample (e.g. tables).
- Support for meta data.