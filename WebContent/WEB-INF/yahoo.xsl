<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:template match="/">
<list>
<xsl:for-each select="//Item">
<item>
<xsl:for-each select="ItemField">
	<xsl:element name="{@TableFieldID}"><xsl:value-of select="@Value"/></xsl:element>
</xsl:for-each>
</item>
</xsl:for-each>
</list>
</xsl:template>
</xsl:stylesheet>
