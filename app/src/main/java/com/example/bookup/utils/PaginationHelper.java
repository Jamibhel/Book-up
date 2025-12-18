package com.example.bookup.utils;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * PaginationHelper - Utility class for handling Firestore pagination
 * 
 * Provides methods to add pagination constraints to Firestore queries
 * and determine if more results are available.
 * 
 * Usage:
 * Query query = db.collection("items");
 * query = PaginationHelper.addPagination(query, PAGE_SIZE, lastVisible);
 * query.get().addOnSuccessListener(...);
 * 
 * @author Senior Developer
 * @version 1.0
 */
public class PaginationHelper {
    
    /**
     * Default page size for paginated queries
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * Minimum page size (to prevent excessive requests)
     */
    private static final int MIN_PAGE_SIZE = 5;
    
    /**
     * Maximum page size (to prevent memory overflow)
     */
    private static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Add pagination to a Firestore query
     * 
     * @param query The base Firestore query
     * @param pageSize Number of items per page (5-100)
     * @param lastVisible The last document from previous page (null for first page)
     * @return Paginated query with LIMIT and startAfter constraints
     */
    public static Query addPagination(Query query, int pageSize, DocumentSnapshot lastVisible) {
        // Validate and normalize page size
        pageSize = normalizePageSize(pageSize);
        
        // Add LIMIT clause
        query = query.limit(pageSize);
        
        // Add cursor for pagination if this isn't the first page
        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }
        
        return query;
    }
    
    /**
     * Check if more results are likely available
     * 
     * Logic: If we got exactly pageSize results, there's likely more data.
     * If we got fewer results, we've reached the end.
     * 
     * @param resultCount Number of results returned
     * @param pageSize Expected page size
     * @return true if more results likely available, false if we've reached end
     */
    public static boolean hasMoreResults(int resultCount, int pageSize) {
        pageSize = normalizePageSize(pageSize);
        return resultCount == pageSize;
    }
    
    /**
     * Validate and normalize page size to acceptable range
     * 
     * @param pageSize Requested page size
     * @return Validated page size (5-100, default 20 if invalid)
     */
    public static int normalizePageSize(int pageSize) {
        if (pageSize < MIN_PAGE_SIZE) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }
    
    /**
     * Get the last document from a query result for pagination
     * 
     * @param documents List of documents returned
     * @return Last document for use in next query, or null if empty
     */
    public static DocumentSnapshot getLastDocument(java.util.List<DocumentSnapshot> documents) {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        return documents.get(documents.size() - 1);
    }
    
    /**
     * Calculate offset-based pagination (alternative to cursor-based)
     * Not recommended for Firestore but provided for reference
     * 
     * Offset-based pagination is inefficient for Firestore (requires reading
     * all documents up to offset). Cursor-based (startAfter) is preferred.
     * 
     * @param pageNumber Page number (1-indexed)
     * @param pageSize Items per page
     * @return Offset to use with limit
     */
    @Deprecated
    public static int calculateOffset(int pageNumber, int pageSize) {
        if (pageNumber < 1) pageNumber = 1;
        pageSize = normalizePageSize(pageSize);
        return (pageNumber - 1) * pageSize;
    }
}
