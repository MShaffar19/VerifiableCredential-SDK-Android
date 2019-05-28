/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { AuthenticationReference, ServiceReference, PublicKey } from './types';
import UserAgentOptions from './UserAgentOptions';
import Identifier from './Identifier';
import UserAgentError from './UserAgentError';
import HostServiceEndpoint from './serviceEndpoints/HostServiceEndpoint';
import UserServiceEndpoint from './serviceEndpoints/UserServiceEndpoint';
const cloneDeep = require('lodash/fp/cloneDeep');

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 */
export default class IdentifierDocument {

  /**
   * The identifier the document represents.
   */
  public id: string;

  /**
   * The date the document was created.
   */
  public created: Date | undefined;

  /**
   * Array of service entries added to the document.
   */
  public publicKeys: PublicKey[] = [];

  /**
   * Array of authentication entries added to the document.
   */
  public authenticationReferences: AuthenticationReference[] = [];

  /**
   * Array of service entries added to the document.
   */
  public serviceReferences: ServiceReference[] = [];

  /**
   * Constructs an instance of the identifier
   * document.
   * @param document from which to create the identifier document.
   * @param options for configuring how to register and resolve identifiers.
   */
  constructor (document: any) {
    // Populate the base properties
    this.id = document.id;
    this.publicKeys = document.publicKeys;

    if (document.created) {
      this.created = new Date(document.created);
    }

    this.authenticationReferences = document.authentication || [];
    this.serviceReferences = document.service || [];

    if (document.service && document.service.length > 0) {
      document.service.forEach((serviceRef: any) => {
        if (serviceRef.serviceEndpoint['@type'] === 'HostServiceEndpoint') {
          serviceRef.serviceEndpoint = HostServiceEndpoint.fromJSON(serviceRef.serviceEndpoint);
        } else {
          serviceRef.serviceEndpoint = UserServiceEndpoint.fromJSON(serviceRef.serviceEndpoint);
        }
      });
    }

  }

  /**
   * Creates a new instance of an identifier document using the
   * provided public keys.
   * @param publicKeys to include in the document.
   */
  public static create (id: string, publicKeys: PublicKey[]): IdentifierDocument {
    const createdDate = new Date(Date.now()).toISOString();
    return new IdentifierDocument({ id: id, created: createdDate, publicKeys: publicKeys });
  }

  /**
   * Creates a new instance of an identifier document using the
   * provided public keys.
   * The id is generated.
   * @param idBase The base id in format did:{method}:{id}. {id} will be filled in by this method
   * @param publicKeys to include in the document.
   * @param options User agent options containing the crypto Api
   */
  public static async createAndGenerateId (idBase: string, publicKeys: PublicKey[], options: UserAgentOptions): Promise<IdentifierDocument> {
    const document = IdentifierDocument.create(idBase, publicKeys);
    const identifier: Identifier = await options.registrar!.generateIdentifier(document);
    document.id = identifier.id;
    return document;
  }

  /**
   * Adds an authentication reference to the document.
   * @param authenticationReference to add to the document.
   */
  public addAuthenticationReference (authenticationReference: AuthenticationReference) {
    this.authenticationReferences.push(authenticationReference);
  }

  /**
   * Adds a service reference to the document.
   * @param serviceReference to add to the document.
   */
  public addServiceReference (serviceReference: ServiceReference) {
    this.serviceReferences.push(serviceReference);
  }

    /**
   * Get Hub Instances from Identity Service Reference.
   */
  public getHubInstances () {
    const filteredServiceReferences = this.serviceReferences.filter(reference => reference.type === 'IdentityHub');
    if (filteredServiceReferences.length === 0 || filteredServiceReferences[0].serviceEndpoint.type !== 'UserServiceEndpoint') {
      throw new UserAgentError(`No Hub Instances for ${this.id}`);
    }
    const serviceEndpoint = <UserServiceEndpoint> filteredServiceReferences[0].serviceEndpoint;
    return serviceEndpoint.instances;
  }

  /**
   * Get Hub Locations from Identity Service Reference.
   */
  public getHubLocations () {
    const filteredServiceReferences = this.serviceReferences.filter(reference => reference.type === 'IdentityHub');
    if (filteredServiceReferences.length === 0 || filteredServiceReferences[0].serviceEndpoint.type !== 'HostServiceEndpoint') {
      throw new UserAgentError(`No Hub Locations for ${this.id}`);
    }
    const serviceEndpoint = <HostServiceEndpoint> filteredServiceReferences[0].serviceEndpoint;
    return serviceEndpoint.locations;

  }

  /**
   * Used to control the the properties that are
   * output by JSON.parse.
   */
  public static fromJSON (obj: any): IdentifierDocument {
    const document = Object.create(IdentifierDocument.prototype);
    const result = Object.assign(document, obj, {
      publicKeys: (<any> obj).publicKey
    });
    delete result.publicKey;
    return new IdentifierDocument(document);
  }

  /**
   * Used to control the the properties that are
   * output by JSON.stringify.
   */
  public toJSON (): any {
    // Clone the current instance. Note the use of
    // a deep clone to ensure immutability of
    // the instance being cloned for serialization
    const clonedDocument = cloneDeep(this);

    // Add the JSON-LD context
    clonedDocument['@context'] = 'https://w3id.org/did/v1';

    // switch authentication references to authentication.
    if (this.authenticationReferences && this.authenticationReferences.length > 0 ) {
      clonedDocument.authentication = this.authenticationReferences;
    }
    clonedDocument.authenticationReferences = undefined;

    // switch service references to service.
    if (this.serviceReferences && this.serviceReferences.length > 0 ) {
      clonedDocument.service = this.serviceReferences;
      clonedDocument.service.forEach((serviceRef: ServiceReference) => {
        if (serviceRef.serviceEndpoint['type'] === 'HostServiceEndpoint') {
          serviceRef.serviceEndpoint = (<HostServiceEndpoint> serviceRef.serviceEndpoint).toJSON();
        } else {
          serviceRef.serviceEndpoint = (<UserServiceEndpoint> serviceRef.serviceEndpoint).toJSON();
        }
      });
    }
    clonedDocument.serviceReferences = undefined;

    if (!this.publicKeys || this.publicKeys.length === 0) {
      clonedDocument.publicKeys = undefined;
    } else {
      clonedDocument.publicKey = this.publicKeys;
      delete clonedDocument.publicKeys;
    }

    // Now return the cloned document for serialization
    return clonedDocument;
  }
}
