/*****************************************
* Array Header Definitions               *
*                                        *
* Stores everything relating to creating *
*     and using dynamic arrays           *
*                                        *
* Created by: Brad Zacher                *
* Computer Science Honours Project 2012  *
* Modified: 20/09/2012                   *
*****************************************/
#ifndef _B_ARRAY_H
#define _B_ARRAY_H

#include <stdlib.h>

size_t * b_element_size_ptr_( void * buf );
size_t * b_length_ptr_( void * buf ); 
void * b_allocate( size_t siz, size_t len ); 
void b_deallocate( void * buf );
size_t b_length( void * buf );
size_t b_bytes( void * buf );

#endif
