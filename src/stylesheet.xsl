<?xml version="1.0" encoding="UTF-8"?>
<html xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xsl:version="1.0">
	<head>
		<title> Rio 2016</title>
	</head>

	<body>
		<div>
			<h1>Rio2016</h1>
			<hr></hr><br></br>

			<xsl:for-each select="body/country">
				<h2><xsl:value-of select="code"/> - <xsl:value-of select="name"/> medalists:</h2>
				<table>
					<tr class="table-head">
						<th><b>Type</b></th>
						<th><b>Sport</b></th>
						<th><b>Category</b></th>
						<th><b>Athlete/Team</b></th>
					</tr>

					<xsl:for-each select="./medal">
						<xsl:sort select="./sport" />

						<tr class="table-row">
							<xsl:if test="./type = 'Gold' ">
								<td class="gold"><xsl:value-of select="type"/></td>
							</xsl:if>
							<xsl:if test="./type = 'Silver' ">
								<td class="silver"><xsl:value-of select="type"/></td>
							</xsl:if>
							<xsl:if test="./type = 'Bronze' ">
								<td class="bronze"><xsl:value-of select="type"/></td>
							</xsl:if>

							<td><xsl:value-of select="sport"/></td>
							<td><xsl:value-of select="category"/></td>
							<td><xsl:value-of select="athlete"/></td>
						</tr>
					</xsl:for-each>
				</table>
				<br></br>
			</xsl:for-each>

		</div>
	</body>

	<style>
		html {
			font-family: "Ubuntu", "Helvetica";
			padding-left: 5px;
		}

		table {
			text-align: center;
			background: #404040;
		}

		.table-row {
			background: #f0f0f0;
			height: 30px;
		}

		.table-head {
			color: #ffffff;
			font-size: 20px;
			height: 30px;
		}

		th {
			min-width:25%;
		}

		.gold   { color: #ffd700; }
		.silver { color: #989898; }
		.bronze { color: #cd7f32; }
	</style>
</html>
