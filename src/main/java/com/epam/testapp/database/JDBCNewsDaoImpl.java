package com.epam.testapp.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.testapp.database.constant.NewsFields;
import com.epam.testapp.database.cp.ConnectionPool;
import com.epam.testapp.database.cp.PooledConnection;
import com.epam.testapp.database.cp.exception.ConnectionSQLException;
import com.epam.testapp.database.cp.exception.NoConnectionException;
import com.epam.testapp.database.exception.DaoException;
import com.epam.testapp.model.News;

//@Service("newsDao")
public class JDBCNewsDaoImpl implements NewsDao
{
	private static final Logger log = Logger.getLogger(JDBCNewsDaoImpl.class);
	@Autowired
	private ConnectionPool cp;
	/**
	 * Private constructor. References this Dao to existing ConnectionPool
	 * @throws DaoException
	 */
	private JDBCNewsDaoImpl() throws DaoException
	{

	}	

	@Override
	public List<News> getList() throws DaoException 
	{
		PooledConnection connection = null;
		PreparedStatement pStatement = null;
		try 
		{
			connection = (PooledConnection) cp.takeConnection();
			pStatement = connection.prepareStatement(NewsDaoStatement.collectNews.getStatement());
			return parseNews(pStatement.executeQuery());
		} 
		catch (NoConnectionException | ConnectionSQLException e) 
		{
			log.error("Can't take connection from ConnectionPool", e);
			throw new DaoException("Can't take connection from ConnectionPool", e);
		} 
		catch (SQLException e) 
		{
			log.error("Error in SQL statement " + NewsDaoStatement.collectNews.getStatement(), e);
			throw new DaoException("Error in SQL statement " + NewsDaoStatement.collectNews.getStatement(), e);
		}
		finally
		{
			if (null != connection)
			{
				try 
				{
					this.closeStatement(pStatement);
					cp.releaseConnection(connection);
				} 
				catch (ConnectionSQLException e) 
				{
					log.error("Can't release connection back", e);
					throw new DaoException("Can't release connection back", e);
				}
			}
		}
	}	

	@Override
	public News fetchById(Integer id) throws DaoException 
	{
		PooledConnection connection = null;
		PreparedStatement pStatement = null;
		try 
		{
			connection = (PooledConnection) cp.takeConnection();
			pStatement = connection.prepareStatement(NewsDaoStatement.collectNewsById.getStatement());
			pStatement.setInt(1, id);
			return parseNewsById(pStatement.executeQuery());
		} 
		catch (NoConnectionException | ConnectionSQLException e) 
		{
			log.error("Can't take connection from ConnectionPool", e);
			throw new DaoException("Can't take connection from ConnectionPool", e);
		} 
		catch (SQLException e) 
		{
			log.error("Error in SQL statement " + NewsDaoStatement.collectNewsById.getStatement(), e);
			throw new DaoException("Error in SQL statement " + NewsDaoStatement.collectNewsById.getStatement(), e);
		}
		finally
		{
			if (null != connection)
			{
				try 
				{
					this.closeStatement(pStatement);
					cp.releaseConnection(connection);
				} 
				catch (ConnectionSQLException e) 
				{
					log.error("Can't release connection back", e);
					throw new DaoException("Can't release connection back", e);
				}
			}
		}
	}

	@Override
	public boolean save(News news) throws DaoException 
	{
		if (null == news)
		{
			return false;
		}
		PooledConnection connection = null;
		PreparedStatement pStatement = null;
		try 
		{
			connection = (PooledConnection) cp.takeConnection();
			pStatement = connection.prepareStatement(NewsDaoStatement.createNews.getStatement(), new String[] {NewsFields.ID.getContent()});
			pStatement.setString(1, news.getTitle());
			pStatement.setString(2, news.getBrief());
			pStatement.setString(3, news.getContent());
			pStatement.setDate(4, news.getDate());
			int result = pStatement.executeUpdate();
			if (result > 0) 
			{
				ResultSet rs = pStatement.getGeneratedKeys();
				if (rs.next()) 
				{
					news.setId(rs.getInt(1));
					return true;
				} 
				else 
				{
					throw new DaoException("Id was not generated");
				}
			}
			else 
			{
				return false;
			}
		} 
		catch (NoConnectionException | ConnectionSQLException e) 
		{
			log.error("Can't take connection from ConnectionPool", e);
			throw new DaoException("Can't take connection from ConnectionPool", e);
		} 
		catch (SQLException e) 
		{
			log.error("Error in SQL statement " + NewsDaoStatement.createNews.getStatement(), e);
			throw new DaoException("Error in SQL statement " + NewsDaoStatement.createNews.getStatement(), e);
		}
		finally
		{
			if (null != connection)
			{
				try 
				{
					this.closeStatement(pStatement);
					cp.releaseConnection(connection);
				} 
				catch (ConnectionSQLException e) 
				{
					log.error("Can't release connection back", e);
					throw new DaoException("Can't release connection back", e);
				}
			}
		}
	}	

	@Override
	public void remove(Integer[] ids) throws DaoException 
	{
		if ((ids == null) || (ids.length < 1))
		{
			return ;
		}
		PooledConnection connection = null;
		PreparedStatement pStatement = null;
		try 
		{
			connection = (PooledConnection) cp.takeConnection();
			StringBuffer query = new StringBuffer(NewsDaoStatement.deleteNews.getStatement());
			int lastId = ids[ids.length - 1];
			for (int id : ids) 
			{
				query.append(id);
				if (lastId == id) 
				{
					query.append(")");
				} 
				else 
				{
					query.append(",");
				}
			}
			pStatement = connection.prepareStatement(query.toString());
			return;
//			return (pStatement.executeUpdate() > 0 ? true : false);
		} 
		catch (NoConnectionException | ConnectionSQLException e) 
		{
			log.error("Can't take connection from ConnectionPool", e);
			throw new DaoException("Can't take connection from ConnectionPool", e);
		} 
		catch (SQLException e) 
		{
			log.error("Error in SQL statement " + NewsDaoStatement.deleteNews.getStatement(), e);
			throw new DaoException("Error in SQL statement " + NewsDaoStatement.deleteNews.getStatement(), e);
		}
		finally
		{
			if (null != connection)
			{
				try 
				{
					this.closeStatement(pStatement);
					cp.releaseConnection(connection);
				} 
				catch (ConnectionSQLException e) 
				{
					log.error("Can't release connection back", e);
					throw new DaoException("Can't release connection back", e);
				}
			}
		}
	}

	@Override
	public boolean updateNews(News news) throws DaoException 
	{
		if (null == news)
		{
			return false;
		}
		PooledConnection connection = null;
		PreparedStatement pStatement = null;
		try 
		{
			connection = (PooledConnection) cp.takeConnection();
			pStatement = connection.prepareStatement(NewsDaoStatement.updateNews.getStatement());
			pStatement.setString(1, news.getTitle());
			pStatement.setString(2, news.getBrief());
			pStatement.setString(3, news.getContent());
			pStatement.setDate(4, news.getDate());
			pStatement.setInt(5, news.getId());
			return (pStatement.executeUpdate() > 0 ? true : false);
		} 
		catch (NoConnectionException | ConnectionSQLException e) 
		{
			log.error("Can't take connection from ConnectionPool", e);
			throw new DaoException("Can't take connection from ConnectionPool", e);
		} 
		catch (SQLException e) 
		{
			log.error("Error in SQL statement " + NewsDaoStatement.updateNews.getStatement(), e);
			throw new DaoException("Error in SQL statement " + NewsDaoStatement.updateNews.getStatement(), e);
		}
		finally
		{
			if (null != connection)
			{
				try 
				{
					this.closeStatement(pStatement);
					cp.releaseConnection(connection);
				} 
				catch (ConnectionSQLException e) 
				{
					log.error("Can't release connection back", e);
					throw new DaoException("Can't release connection back", e);
				}
			}
		}
	}

	/**
	 * Parses list of News from the ResultSet using enums from NewsConstants.
	 * @param rs ResultSet to be parsed.
	 * @return List of compiled News beans.
	 * @throws DaoException
	 */
	private List<News> parseNews(ResultSet rs) throws DaoException
	{
		List<News> newsList = new ArrayList<News>();
		if (rs != null)
		{
			try 
			{
				while(rs.next())
				{
					News news = new News();
					news.setBrief(rs.getString(NewsFields.BRIEF.getContent()));
					news.setContent(rs.getString(NewsFields.CONTENT.getContent()));
					news.setDate(rs.getDate(NewsFields.DATE.getContent()));
					news.setTitle(rs.getString(NewsFields.TITLE.getContent()));
					news.setId(rs.getInt(NewsFields.ID.getContent()));
					newsList.add(news);				
				}			
			} 
			catch (SQLException e) 
			{
				log.error("Error in trying to parse News through resultSet", e);
				throw new DaoException("Error in trying to parse News through resultSet", e);
			}
		}
		return newsList;		
	}

	private News parseNewsById(ResultSet rs) throws DaoException
	{
		if (rs != null)
		{
			try 
			{
				while(rs.next())
				{
					News news = new News();
					news.setBrief(rs.getString(NewsFields.BRIEF.getContent()));
					news.setContent(rs.getString(NewsFields.CONTENT.getContent()));
					news.setDate(rs.getDate(NewsFields.DATE.getContent()));
					news.setTitle(rs.getString(NewsFields.TITLE.getContent()));
					news.setId(rs.getInt(NewsFields.ID.getContent()));
					return news;				
				}			
			} 
			catch (SQLException e) 
			{
				log.error("Error in trying to parse News through resultSet", e);
				throw new DaoException("Error in trying to parse News through resultSet", e);
			}
		}
		return null;	
	}
	/**
	 * Method that contains logic of closing PreparedStatement.
	 * @param pStatement the PreparedStatement to be closed.
	 */
	private void closeStatement(PreparedStatement pStatement)
	{
		try 
		{
			pStatement.close();
		} 
		catch (SQLException e) 
		{
			log.error("Error during trying to close pStatement", e);
		}
	}

}
