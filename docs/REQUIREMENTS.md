
# Helodali Requirements

The application serves generally as a cloud-based storage of high-res images associated with artwork. The artwork has properties 
associated with it and references to purchases, exhibitions, press.

### Data Types

The following basic data types are needed.

####Artwork
Define and optionally upload an image representing artwork. The image can potentially be several gbs in size and have
the following properties. The only required property is Year, which is bolded.
  * Title - String(512) - A string of maximum length 512 characters
  * Image - A data structure containing everything necessary to define an image location. Also include the image
            metadata properties:
    * Size - An integer number, size of image file in bytes
    * Space - A string(128) describing the color space
    * height - Integer height in pixels
    * Width - Integer width in pixels
    * Density - Integer pixels per inch density
    * Format - String(128), e.g. "png"
  * Description - String(10K)
  * __Year__ - Four-digit (default to current year)
  * Status - Member of { sold private destroyed not-for-sale for-sale }
  * Type - Member of { architecture books collage computer digital drawings
                       film-video installation mixed-media mural painting performance
                       photography prints sculpture wall-relief works-on-paper }
  * Medium - String(256)
  * Dimensions - String(64)
  * Condition - String(512)
  * List Price - Number, can be zero and have two-digit precision
  * Exhibitions - Multi-valued of uuid references to exhibitions (defined below)
  * Purchases - List of purchases. There will typically be a single value in the list of; multiple values 
                are applicable in the context of prints or tracking secondary sales.
    A Purchase has the following properties:
    * __Price__ - A number stored with at most two-digit precision 
    * Buyer - A uuid reference to a contact (defined below), nil for unknown buyer (.e.g bought through dealer)
    * Donated - Boolean representing a donated artwork
    * Commissioned - Boolean
    * Collection - Boolean
    * Agent - A uuid reference to a contact, nil for no agent involvement
    * Dealer - A uuid reference to a contact, nil for no dealer involvement
    * Date - A Date with day-level granularity
    * Total-commission-percent - Number between 0 and 100, default to 0 when value non-existent
    * Location - String(128)
    * On-public-display - Boolean
    * Notes - String(10K)

The uploaded image should be stored securely with only the user having access.

Artwork item creation should be achieved either manually or through import from Instagram. The Instagram import
should capture and store the following with the artwork:
  * The image - The Instagram image should be copied and placed in the helodali image store
  * Instagram ID - The unique id of the Instagram post
  * Caption - String(1024), the caption of the Instagram post
  * Likes Count - The integer number of likes

#### Exhibitions
Exhibitions are a means to group artwork items and assign properties to the grouping. The exhibition may represent
  a show which the artist took part in or simply be an arbitrary grouping. Exhibitions have the following properties.
  * __Name__ - String(128)
  * __Begin-Date__ - Date value, e.g. YYYY/MM/DD
  * End-Date - Date value
  * Kind - member of { solo group duo other }
  * Location - String(256) 
  * URL - String(512)
  * Notes - String(10K)
  * Include-in-CV - Boolean to indicate whether this exhibition is something to include in the artist's CV.
  * Associated-Documents - Multi-valued references to documents (defined below)
  * Associated-Press - Multi-valued references to press releases (defined below)
  
#### Contacts
A contact represents a person, company, or institution. The following properties are required:
  *  __Name__ - String(128)
  * Email - String(128)
  * Phone - String(64)
  * Instagram - String(64), an Instagram username
  * Facebook - String(64), a FB username
  * Address - String(1024), free form multi-line text
  * Notes - String(10K)

#### Press
Press can represent a document or URL which the artist would like to keep on file. Typically, this document or webpage will
contain a review of the artist's work.
  * Title - String(512)
  * Author - String(128)
  * Publication - String)128)
  * Volume - String(128)
  * URL - String(512)
  * Publication-Date - Date value, day-level
  * Page-Numbers - String(128)
  * Include-in-CV - Boolean
  * Associated-Documents - Multi-valued references to documents (defined below)
  * Notes - String(10K)

#### Expenses
Expenses are items with the following properties:
  * __Price__ - Number, can be zero and have two-digit precision
  * __Date__ - A Date with day-level granularity
  * Expense-Type - Member of { materials advertising packaging-shipping other
                               miscellaneous dues-subscriptions})
  * Notes - String(10K)

#### Documents
Arbitrary documents can be uploaded and stored in helodali. These documents can be later linked to from other
items such as Press or Exhibitions. Properties are:
  * Filename - String(512)
  * Size - An integer number, size of image file in bytes
  * Notes - String(10K)
  
#### Artist Profile
Some basic information describing the artist is stored:
  * Name - Artist's working name
  * Email - String(128)
  * Phone - String(64)
  * URL - String(512)
  * Photo - Link to artist photo
  * Birth-Year - Integer, four digits
  * Birth-Place - String(128)
  * Currently-Resides - String(128)
  * Degrees - List of academic degrees, each of which defined by:
    * Year - Integer, four digit
    * Description - String(128)
  * Awards-and-Grants - List of awards and grants, each of which defined by:
    * Year - Integer, four digit
    * Description - String(128)
  * Residences - List of residences, each of which defined by:
    * Year - Integer, four digit
    * Description - String(128)
  * Lecture-and-Talks - List of collections which have obtained the artist's work, each of which defined by:
    * Year - Integer, four digit
    * Description - String(128)
  * Collections - List of collections which have obtained the artist's work, each of which defined by:
    * Description - String(128)
    

