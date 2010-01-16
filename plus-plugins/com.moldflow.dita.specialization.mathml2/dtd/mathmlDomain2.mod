<?xml version="1.0" encoding="UTF-8"?>

<!--
    <public publicId="-//Moldflow//ELEMENTS DITA 1.1 MathML Domain 2//EN"  uri="mathmlDomain2.mod"/>
    <system systemId="http://www.moldflow.com/schema/dtd/dita/1.1/mathml/2/mathmlDomain2.mod" uri="mathmlDomain2.mod"/>
-->

<!-- included MathML document type -->
<!ENTITY % MATHML.prefixed "INCLUDE">
<!ENTITY % MATHML.prefix "mml">
<!ENTITY % MathMLstrict "INCLUDE">
<!ENTITY % mathml 
PUBLIC "-//W3C//DTD MathML 2.0//EN"
      "http://www.w3.org/Math/DTD/mathml2/mathml2.dtd" >
%mathml;

<!ENTITY % math "math">
<!ENTITY % mathph "mathph">
<!ENTITY % equation "equation">

<!ELEMENT mathph (%math.qname;)>
<!ATTLIST mathph
  %univ-atts;
  outputclass CDATA #IMPLIED
  xmlns:mml CDATA #FIXED "http://www.w3.org/1998/Math/MathML">

<!ELEMENT math (%math.qname;)>
<!ATTLIST math
  %univ-atts;
  outputclass CDATA #IMPLIED
  xmlns:mml CDATA #FIXED "http://www.w3.org/1998/Math/MathML"> 

<!ELEMENT equation ((%title;)?, (%desc;)?, (%math;)) >
<!ATTLIST equation
  %univ-atts;
  %display-atts;
  outputclass CDATA #IMPLIED
>

<!ATTLIST mathph %global-atts; class CDATA "+ topic/foreign math-d/mathph ">
<!ATTLIST math %global-atts; class CDATA "+ topic/foreign math-d/math " >
<!ATTLIST equation %global-atts; class CDATA "+ topic/fig math-d/equation " >

