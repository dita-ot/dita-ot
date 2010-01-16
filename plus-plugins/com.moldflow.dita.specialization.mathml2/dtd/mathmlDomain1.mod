<?xml version="1.0" encoding="UTF-8"?>

<!--
    <public publicId="-//Moldflow//ELEMENTS DITA 1.1 MathML Domain 1//EN"  uri="mathmlDomain1.mod"/>
    <system systemId="http://www.moldflow.com/schema/dtd/dita/1.1/mathml/1/mathmlDomain1.mod" uri="mathmlDomain1.mod"/>
-->

<!-- declaration for the specialized wrapper and alternate element -->
<!ENTITY % mathML "mathml">

<!-- included MathML document type -->
<!ENTITY % MATHML.prefixed "INCLUDE">
<!ENTITY % MATHML.prefix "mml">
<!ENTITY % MathMLstrict "INCLUDE">
<!ENTITY % mathml 
PUBLIC "-//W3C//DTD MathML 2.0//EN"
      "http://www.w3.org/Math/DTD/mathml2/mathml2.dtd" >
%mathml;

<!-- definition for the specialized wrapper and alternate element -->
<!ELEMENT mathph (%math.qname;)>
<!ATTLIST mathph %global-atts;
class CDATA "+ topic/foreign math-d/mathph "
xmlns:mml CDATA #FIXED "http://www.w3.org/1998/Math/MathML"> 

<!ELEMENT math (%math.qname;)>
<!ATTLIST math %global-atts;
class CDATA "+ topic/foreign math-d/math "
xmlns:mml CDATA #FIXED "http://www.w3.org/1998/Math/MathML"> 

