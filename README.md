"# beback" 
<h1>Be Back</h1>
With this application, you will remember who the hell you lended this stuff to/ who the hell you borrowed it from/ or what stuff I'm waiting for.<br/>
In other words, you can record loans and delivery on this small app.

<h1>Screenshots</h1>
<img src="readme_material/screenshot1.gif" width="200">
<img src="readme_material/screenshot2.gif" width="200">
<img src="readme_material/screenshot3.gif" width="200">

<h1>Motivation</h1>
This application has been designed as a free project required for an android training.<br/>
I enjoyed the occasion to create an app that would fill a need that I had (and some of my acquaintances).<br/>
Let's face the truth, it won't transform the world.<br/>
But still useful to me and other folks !

<h1>Features</h1>
Allows to record loans and expected deliveries.<br/>
- Add : add a loan/delivery entry<br/>
- Delete : delete a specific loan/delivery entry<br/>
- Archive : set a loan/delivery as returned/received<br/>
- Filter : filter entries following simple criterias<br/>
- View : by item / by person / current items / archived items<br/>

3 langages available : english/french(génial !)/spanish(estupendo !)

<h1>How to use the app ?</h1>

**Displaying loans** :
- click the central bottom button to switch view between view by item and view by person
- click the "archive" top bottom to switch view between current loans and archived ones
<br/><img src="readme_material/video_beback_loan_display.gif" width="200">

**Deleting loan** (remove it definitely from list) :
- swipe to the right
- you can undo the deletion of an item clicking on the Undo button in the bottom snackbar (displayed for 3s)
<br/><img src="readme_material/video_beback_delete.gif" width="200">

**Archiving loan** (set it as "returned/received") :
- swipe to the left
- you can undo the archiving of an item clicking on the Undo button in the bottom snackbar (displayed for 3s)
<br/><img src="readme_material/video_beback_archiving.gif" width="200">

**Filtering loans**
- click the Filter button
- choose the paramater to filter your request and click the submit button
- you can navigate accross the differents views (by object/by person/pending loans/archived loans) with the filter enabled
- click the "Erase Filters" button to
<br/><img src="readme_material/video_beback_delete.gif" width="200">

<h1>Technical considerations</h1>
These libraries have been used to develop this app :<br/>
Quick permissions :     implementation 'com.github.quickpermissions:quickpermissions-kotlin:0.4.0'<br/>
Flexbox :     implementation 'com.google.android:flexbox:2.0.1'<br/>
Commons :     implementation 'org.apache.commons:commons-text:1.7'

Firebase has been used for authentication and Firebase Firestore to save/store data.

<h1>Credits</h1>
The following resources have been used to make this app nicer :<br/>
Icons made by <a href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a><br/>
Icons made by <a href="https://www.flaticon.com/authors/vectors-market" title="Vectors Market">Vectors Market</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a>
