<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
<!--  ================ TOPIC MOD =====================  -->
  
   <xs:redefine schemaLocation="topic.grp">
    <xs:group name="keyword">
      <xs:choice>
          <xs:group ref="keyword"/>
          <xs:group ref="pr-d-keyword" />
          <xs:group ref="ui-d-keyword" />
          <xs:group ref="ut-d-keyword" />
          <xs:group ref="sw-d-keyword" />
      </xs:choice>
    </xs:group>
    
     <xs:group name="ph">
      <xs:choice>
          <xs:group ref="ph"/>
          <xs:group ref="pr-d-ph" />
          <xs:group ref="ui-d-ph" />
          <xs:group ref="hi-d-ph" />
          <xs:group ref="ut-d-ph" />
          <xs:group ref="sw-d-ph" />
      </xs:choice>
     </xs:group>
     
     <xs:group name="pre">
      <xs:choice>
          <xs:group ref="pre"/>
          <xs:group ref="pr-d-pre" />
          <xs:group ref="ui-d-pre" />
          <xs:group ref="sw-d-pre" />
      </xs:choice>
     </xs:group>
     
     <xs:group name="figgroup">
      <xs:choice>
          <xs:group ref="figgroup"/>
          <xs:group ref="pr-d-figgroup" />
          <xs:group ref="ut-d-figgroup" />
      </xs:choice>
     </xs:group>
     
     <xs:group name="xref">
      <xs:choice>
          <xs:group ref="xref"/>
          <xs:group ref="pr-d-xref" />
      </xs:choice>
     </xs:group>
     
     <xs:group name="fn">
      <xs:choice>
          <xs:group ref="fn"/>
          <xs:group ref="pr-d-fn" />
      </xs:choice>
     </xs:group>
     
    <xs:group name="dl">
    <xs:choice>
        <xs:group ref="dl"/>
        <xs:group ref="pr-d-dl"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="dlentry">
    <xs:choice>
        <xs:group ref="dlentry"/>
        <xs:group ref="pr-d-dlentry"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="dt">
    <xs:choice>
        <xs:group ref="dt"/>
        <xs:group ref="pr-d-dt"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="dd">
    <xs:choice>
        <xs:group ref="dd"/>
        <xs:group ref="pr-d-dd"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="fig">
    <xs:choice>
        <xs:group ref="fig"/>
        <xs:group ref="pr-d-fig"/>
        <xs:group ref="ut-d-fig" />
    </xs:choice>
  </xs:group >     
 
 </xs:redefine>
 
 
</xs:schema>