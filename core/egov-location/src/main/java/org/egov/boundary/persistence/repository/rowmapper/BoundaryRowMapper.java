package org.egov.boundary.persistence.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.boundary.domain.model.Boundary;
import org.egov.boundary.web.contract.BoundaryType;
import org.egov.boundary.web.contract.HierarchyType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class BoundaryRowMapper implements RowMapper<Boundary> {

	@Override
	public Boundary mapRow(final ResultSet rs, final int rowNum) throws SQLException {

		BoundaryType boundaryType = BoundaryType.builder().id(String.valueOf(rs.getLong("btId"))).hierarchy(rs.getLong("btHierarchy"))
				.name(rs.getString("btName")).createdBy(rs.getLong("btCreatedBy"))
				.lastModifiedBy(rs.getLong("btLastModifiedBy")).createdDate(rs.getDate("btCreatedDate").getTime())
				.lastModifiedDate(rs.getDate("btLastModifiedDate").getTime()).localName(rs.getString("btLocalName"))
				.code(rs.getString("btCode")).tenantId(rs.getString("btTenantId")).build();
		
		HierarchyType hierarchyType =new HierarchyType();
		hierarchyType.setId(rs.getLong("btHierarchyType"));
		boundaryType.setHierarchyType(hierarchyType);

		final Boundary boundary = Boundary.builder().id(rs.getLong("bId")).name(rs.getString("bName")).localName(rs.getString("bLocalName"))
				.boundaryNum(rs.getLong("bBoundaryNum")).fromDate(rs.getDate("bFromdate")).toDate(rs.getDate("bToDate"))
				.bndryId(rs.getLong("bBndryid")).materializedPath(rs.getString("bMaterialiedPath")).code(rs.getString("bCode"))
				.isHistory(rs.getBoolean("bHistory")).createdDate(rs.getDate("bCreatedDate").getTime())
				.lastModifiedDate(rs.getDate("bLastModifiedDate").getTime()).createdBy(rs.getLong("bCreatedBy"))
				.lastModifiedBy(rs.getLong("bLastModifiedBy")).tenantId(rs.getString("bTenantId"))
				.boundaryType(boundaryType).build();

		if (rs.getFloat("bLongitude") != 0) {
			boundary.setLongitude(rs.getFloat("bLongitude"));
		}
		if (rs.getFloat("bLatitude") != 0) {
			boundary.setLatitude(rs.getFloat("bLatitude"));
		}
		if (rs.getLong("bParent") != 0) {
			Boundary parent = Boundary.builder().id(rs.getLong("bParent")).build();
			boundary.setParent(parent);
		}
		return boundary;

	}

}
