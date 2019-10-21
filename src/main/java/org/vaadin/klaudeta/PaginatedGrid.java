package org.vaadin.klaudeta;

import java.util.List;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.function.SerializableComparator;

/**
 * Grid component where scrolling feature is replaced with a pagination
 * component.
 *
 * @author klau
 *
 * @param <T>
 */
public class PaginatedGrid<T> extends Grid<T> implements DataProviderListener<T> {
	private static final long serialVersionUID = -4888139726692291021L;

	private PlutoniumPagination paginaton;

	private DataProvider<T, ?> dataProvider;

	public PaginatedGrid() {
		paginaton = new PlutoniumPagination();
		this.setHeightByRows(true);
		paginaton.addPageChangeListener(e -> doCalcs(e.getNewPage()));
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		getParent().ifPresent(p -> {
			int indexOfChild = p.getElement().indexOfChild(this.getElement());
			Span wrapper = new Span(paginaton);
			wrapper.getElement().getStyle().set("width", "100%");
			p.getElement().insertChild(indexOfChild + 1, wrapper.getElement());
		});

		doCalcs(0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doCalcs(int newPage) {
		int offset = newPage > 0 ? (newPage - 1) * this.getPageSize() : 0;

		InnerQuery query = new InnerQuery<>(offset);

		paginaton.setTotal(dataProvider.size(query));

		super.setDataProvider(DataProvider.fromStream(dataProvider.fetch(query)));
	}

	@Override
	public void setPageSize(int pageSize) {
		super.setPageSize(pageSize);
		if (paginaton != null) {
			paginaton.setPageSize(pageSize);
			paginaton.setPage(1);
			doCalcs(paginaton.getPage());
		}
	}

	public void setPage(int page) {
		paginaton.setPage(page);
	}

	@Override
	public void setHeightByRows(boolean heightByRows) {
		super.setHeightByRows(true);
	}

	/**
	 * Sets the count of the pages displayed before or after the current page.
	 * 
	 * @param size
	 */
	public void setPaginatorSize(int size) {
		paginaton.setPage(1);
		paginaton.setPaginatorSize(size);
		paginaton.refresh();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setDataProvider(DataProvider<T, ?> dataProvider) {
		this.dataProvider = dataProvider;
		dataProvider.addDataProviderListener(this);
		InnerQuery query = new InnerQuery<>();

		paginaton.setTotal(dataProvider.size(query));

		super.setDataProvider(DataProvider.fromStream(dataProvider.fetch(query)));
	}

	@Override
	public void onDataChange(DataChangeEvent<T> event) {
		// TODO Is it possible to check if the data changed by event is within the current page?
		// TODO Check that this works for both single data and whole dataset change
		doCalcs(paginaton.getPage());
	}

	private class InnerQuery<F> extends Query<T, F> {
		private static final long serialVersionUID = -6894002015037367722L;

		InnerQuery() {
			this(0);
		}

		InnerQuery(int offset) {
			super(offset, getPageSize(), getDataCommunicator().getBackEndSorting(),
					getDataCommunicator().getInMemorySorting(), null);
		}

		@SuppressWarnings("unused")
		InnerQuery(int offset, List<QuerySortOrder> sortOrders, SerializableComparator<T> serializableComparator) {
			super(offset, getPageSize(), sortOrders, serializableComparator, null);
		}
	}
}
