/****************************************
* Array Function Defs                   *
* Created by: Brad Zacher               *
* Computer Science Honours Project 2012 *
* Modified: 20/09/2012                  *
****************************************/
#ifndef B_ARRAY_CPP
#define B_ARRAY_CPP

#include "b_array.h"

size_t * b_element_size_ptr_( void * buf )
{
    return ( size_t * )( ( char* )buf - sizeof( size_t ) );
}
 
size_t * b_length_ptr_( void * buf )
{
    return ( size_t * )( ( char * )buf - ( sizeof( size_t ) << 1 ) );
}
 
void * b_allocate( size_t siz, size_t len )
{
    char * buf = (char *)malloc( siz * len + ( sizeof( size_t ) << 1 ) );
    if( buf )
    {
        buf += sizeof( size_t ) << 1;
        (*b_element_size_ptr_( buf )) = siz;
        (*b_length_ptr_( buf )) = len;
    }
    return buf;
}
 
void b_deallocate( void * buf )
{
    if( buf )
        free( b_length_ptr_( buf ) );
}
 
size_t b_length( void * buf )
{
    return *b_length_ptr_( buf );
}
 
size_t b_bytes( void * buf )
{
    return b_length( buf ) * (*b_element_size_ptr_( buf ));
}

#endif
