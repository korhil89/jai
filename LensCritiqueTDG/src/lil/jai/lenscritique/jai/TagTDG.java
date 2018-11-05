package lil.jai.lenscritique.jai;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ili.jai.lenscritique.data.Tag;
import ili.jai.tdg.api.AbstractTDG;
import ili.jai.tdg.api.TDGRegistry;

public class TagTDG extends AbstractTDG<Tag> {

	private Connection conn;
	private static final String CREATE = "CREATE TABLE Tag (ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, LABEL VARCHAR(100) NOT NULL";
	private static final String DROP = "DROP TABLE Tag";
	private static final String FIND_BY_ID = "SELECT ID,LABEL FROM Tag t WHERE t.ID=?";
	private static final String INSERT = "INSERT INTO Tag (LABEL) VALUES(?)";
	private static final String UPDATE = "UPDATE Tag t SET t.LABEL = ? WHERE t.ID = ?";
	private static final String DELETE = "DELETE FROM Tag t WHERE t.ID = ?";
	private static final String WHERE = "SELECT ID FROM TAG t WHERE ";
	private static final String ALL = "SELECT ID FROM TAG";

	@Override
	public void createTable() throws SQLException {
		try (Statement stm = conn.createStatement()) {
			stm.executeUpdate(CREATE);
		}

	}

	@Override
	public void deleteTable() throws SQLException {
		try (Statement stm = conn.createStatement()) {
			stm.executeUpdate(DROP);
		}

	}

	@Override
	public List<Tag> selectWhere(String clauseWhereWithJoker, Object... args) throws SQLException {
		List<Tag> result = new ArrayList<>();
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(WHERE + clauseWhereWithJoker)) {
			int index = 1;
			for (Object arg : args) {
				pst.setObject(index++, arg);
			}
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					result.add(findById(rs.getLong(1)));
				}
			}
		}
		return result;
	}

	@Override
	protected Tag retrieveFromDB(long id) throws SQLException {
		Tag t = null;
		try (PreparedStatement pst = conn.prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, id);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					t = new Tag();
					t.setId(rs.getLong(1));
					t.setLabel(rs.getString(2));

				}
			}
		}
		return t;
	}

	@Override
	protected Tag insertIntoDB(Tag t) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			pst.setString(1, t.getLabel());
			int result = pst.executeUpdate();
			assert result == 1;
			try (ResultSet keys = pst.getGeneratedKeys()) {
				if (keys.next()) {
					t.setId(keys.getLong(1));
				}
			}
			return t;
		}
	}

	@Override
	protected Tag updateIntoDB(Tag t) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(UPDATE)) {
			assert findById(t.getId()) == t;
			pst.setString(1, t.getLabel());
			pst.setLong(2, t.getId());
			int result = pst.executeUpdate();
			assert result == 1;
			return t;
		}
	}

	@Override
	protected Tag refreshFromDB(Tag t) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, t.getId());
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					t.setId(rs.getLong(1));
					t.setLabel(rs.getString(2));
				}
			}
		}
		return t;

	}

	@Override
	protected Tag deleteFromDB(Tag t) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(DELETE)) {
			assert findById(t.getId()) == t;
			pst.setLong(1, t.getId());
			int result = pst.executeUpdate();
			assert result == 1;
			return t;
		}
	}

	@Override
	protected List<Long> findAllIds() throws SQLException {
		List<Long> result = new ArrayList<>();
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(ALL)) {
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					result.add(rs.getLong(1));
				}
			}
		}
		return result;
	}



}
