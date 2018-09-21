/*
Serval DNA MDP lookup service
Copyright (C) 2016-2017 Flinders University
Copyright (C) 2010-2015 Serval Project Inc.
Copyright (C) 2010-2012 Paul Gardner-Stephen
 
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

#include "serval.h"
#include "conf.h"
#include "keyring.h"
#include "overlay_buffer.h"
#include "debug.h"
#include "mdp_services.h"

int parseDnaReply(const char *buf, size_t len, char *token, char *did, char *name, char *uri, const char **bufp)
{
  /* Replies look like: TOKEN|URI|DID|NAME| where TOKEN is usually a hex SID */
  const char *b = buf;
  const char *e = buf + len;
  char *p, *q;
  for (p = token, q = token + SID_STRLEN; b != e && *b != '|' && p != q; ++p, ++b)
    *p = *b;
  *p = '\0';
  if (b == e || *b++ != '|')
    return 0;
  for (p = uri, q = uri + 511; b != e && *b != '|' && p != q; ++p, ++b)
    *p = *b;
  *p = '\0';
  if (b == e || *b++ != '|')
    return 0;
  for (p = did, q = did + DID_MAXSIZE; b != e && *b != '|' && p != q; ++p, ++b)
    *p = *b;
  *p = '\0';
  if (b == e || *b++ != '|')
    return 0;
  for (p = name, q = name + ID_NAME_MAXSIZE; b != e && *b != '|' && p != q; ++p, ++b)
    *p = *b;
  *p = '\0';
  if (b == e || *b++ != '|')
    return 0;
  if (bufp)
    *bufp = b;
  return 1;
}

DEFINE_BINDING(MDP_PORT_DNALOOKUP, overlay_mdp_service_dnalookup);
static int overlay_mdp_service_dnalookup(struct internal_mdp_header *header, struct overlay_buffer *payload)
{
  IN();
  char did[DID_MAXSIZE + 1];
  
  size_t pll = ob_remaining(payload);
  if (pll < 1)
    RETURN(WHY("Empty DID in DNA resolution request"));
  if (pll > sizeof did - 1)
    pll = sizeof did - 1;
  ob_get_bytes(payload, (unsigned char *)did, pll);
  did[pll] = '\0';
  
  DEBUGF(mdprequests, "MDP_PORT_DNALOOKUP did=%s", alloca_str_toprint(did));
  
  int results=0;
  keyring_iterator it;
  keyring_iterator_start(keyring, &it);
  while(keyring_find_did(&it, did))
    {
      const char *unpackedDid = (const char *) it.keypair->private_key;

      /* package DID and Name into reply (we include the DID because
	 it could be a wild-card DID search, but the SID is implied 
	 in the source address of our reply). */
      if (strlen(unpackedDid) > DID_MAXSIZE)
	/* skip excessively long DID records */
	continue;
      
      const char *name = (const char *)it.keypair->public_key;
      struct subscriber *subscriber = it.identity->subscriber;
      // URI is sid://SIDHEX/local/DID
      strbuf b = strbuf_alloca(SID_STRLEN + DID_MAXSIZE + 20);
      strbuf_puts(b, "sid://");
      strbuf_tohex(b, SID_STRLEN, subscriber->sid.binary);
      strbuf_puts(b, "/local/");
      strbuf_puts(b, unpackedDid);
      overlay_mdp_dnalookup_reply(header->source, header->source_port, subscriber, strbuf_str(b), unpackedDid, name);
      results++;
    }
  if (!results) {
    /* No local results, so see if servald has been configured to use
       a DNA-helper that can provide additional mappings.  This provides
       a generalised interface for resolving telephone numbers into URIs.
       The first use will be for resolving DIDs to SIP addresses for
       OpenBTS boxes run by the OTI/Commotion project. 
       
       The helper is run asynchronously, and the replies will be delivered
       when results become available, so this function will return
       immediately, so as not to cause blockages and delays in servald.
    */
    CALL_TRIGGER(dna_lookup, header, did);
  }
  RETURN(0);
}

// Put a dummy no-op trigger callback into the "cmd_cleanup" trigger section,
static void __dummy_dna_lookup(struct internal_mdp_header *UNUSED(header), const char *UNUSED(did)){}
DEFINE_TRIGGER(dna_lookup, __dummy_dna_lookup);
