const customerType = document.getElementById('customerType');
    const city = document.getElementById('city');
    const customerName = document.getElementById('customerName');
    const env = document.getElementById('env');
    const mobilePrefix = document.getElementById('mobilePrefix');
   const inputs = document.querySelectorAll('#customerName, #noOfLeads, #mobilePrefix, #env, #customerType, #city');
   const runBtn = document.getElementById('runBtn');
   const noOfLeads =  document.getElementById('noOfLeads')
   const showroomContainer = document.getElementById('showroomContainer');
   const showroom = document.getElementById('showroom');

     const cityData = {
                   "DC": {
                     "Bengaluru": "1"
                   },
                   "HL": {
                     "Bengaluru": "1",
                     "Hyderabad": "8",
                     "Chennai": "2",
                     "Mumbai": "3",
                     "Thane": "11",
                     "Pune": "10",
                     "Mysore": "14",
                     "Coimbatore": "27",
                     "Kolkata": "4",
                     "Ahmedabad": "38",
                     "Visakhapatnam": "6",
                     "Surat": "31",
                     "Gurgaon": "9",
                     "New Delhi": "7",
                     "Noida": "26",
                     "Bhubaneswar": "28",
                     "Jaipur": "37",
                     "Lucknow": "12",
                     "Kochi": "5",
                     "Nagpur": "30",
                     "Madurai": "35",
                     "Mangalore": "13",
                     "Nashik": "34",
                     "Patna": "15",
                     "Ranchi": "32",
                     "Salem": "29",
                     "Vijayawada": "18",
                     "Trichy": "36"
                   },
                   "HFN": {
                     "Hyderabad": "8",
                     "Kolkata": "4",
                     "Jamshedpur": "25",
                     "Coimbatore": "27",
                     "Vijayawada": "18",
                     "Tirupati": "16",
                     "Mysore": "14",
                     "Chennai": "2",
                     "Bengaluru": "1",
                     "Guwahati": "17",
                     "Patna": "15",
                     "Trivandrum": "22",
                     "Siliguri": "21",
                     "Warangal": "23",
                     "Visakhapatnam": "6",
                     "Nizamabad": "19",
                     "Karimnagar": "24",
                     "Mangalore": "13",
                     "Shimgoa": "20",
                     "Kochi": "5",
                     "Ranchi": "3",
                     "Ahmedabad": "38",
                     "Thane": "11",
                     "Nagpur": "30"
                   }
                 }


    document.addEventListener("DOMContentLoaded", () => {
      const mobilePrefixInput = mobilePrefix

      mobilePrefixInput.addEventListener("input", () => {
        const value = parseInt(mobilePrefixInput.value, 10);

        // Limit to 2 digits
        if (mobilePrefixInput.value.length > 2) {
          mobilePrefixInput.value = mobilePrefixInput.value.slice(0, 2);
        }

        // Validate range 60–99
        if (isNaN(value) || value < 60 || value > 99) {
          mobilePrefixInput.classList.add("invalid");
          mobilePrefixInput.setCustomValidity("Prefix must be between 60 and 99");
        } else {
          mobilePrefixInput.classList.remove("invalid");
          mobilePrefixInput.setCustomValidity("");
        }
      });
    });




   customerType.addEventListener('change', () => {
       const selectedType = customerType.value;
       const cities = cityData[selectedType] || {};

       city.innerHTML = '<option value="">Select City</option>';
       Object.keys(cities).forEach(cityName => {
           const opt = document.createElement('option');
           opt.value = cities[cityName];
           opt.text = cityName;
           city.add(opt);
       });

    //    validateForm();
   });


async function updateShowRoom() {
  const selectedType = customerType.value;
  console.log("Selected Customer Type:", selectedType);

  // Show showroom only for HL or HFN
  if (selectedType === 'HL' || selectedType === 'HFN') {
    showroomContainer.style.display = 'block';
  } else {
    showroomContainer.style.display = 'none';
    showroom.value = ''; // reset showroom if hidden
    return; // ⛔ stop here if DC or others
  }

   const selectedCityText = city.options[city.selectedIndex]?.text?.trim() || "";
   console.log("Selected City Text:", selectedCityText);

  // Prepare request payload
  const payload = {
    customerType: selectedType,
    environment: env.value.trim(),
    city: selectedCityText,
  };

  console.log("📦 Payload:", payload);

  try {
      let apiBaseUrl = window.location.origin;
      const configResponse = await fetch('/api/helper/loadConfig');
      if (configResponse.ok) {
        const config = await configResponse.json();
        if (config.apiBaseUrl) apiBaseUrl = config.apiBaseUrl;
      }

      const response = await fetch(`${apiBaseUrl}/getShowroomsList`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const data = await response.json();

      // ✅ Populate dropdown
      if (Array.isArray(data.showrooms) && data.showrooms.length > 0) {
        showroom.innerHTML = `<option value="">Select showroom</option>`;
        data.showrooms.forEach(item => {  
          const name = item.showroom_name || item.name || 'Unnamed';
          const sf_id = item.live_sf_id|| 'N/A';
          const option = document.createElement("option");
          option.value =sf_id;
          option.textContent = name;
          showroom.appendChild(option);
        });
      } else {
      }

    } catch (err) {
      console.error('❌ Failed to load showrooms:', err);
      showroom.innerHTML = `<option value=""></option>`;
    }

}


customerType.addEventListener('change', updateShowRoom);
env.addEventListener('change',updateShowRoom);


// Disable initially
runBtn.disabled = true;

// Enable only when all inputs are filled
function checkInputs() {
    const allFilled = Array.from(inputs).every(input => input.value.trim() !== '');
    if (allFilled) {
        runBtn.classList.add('active');
        runBtn.disabled = false;
    } else {
        runBtn.classList.remove('active');
        runBtn.disabled = true;
    }
}

inputs.forEach(input => {
    input.addEventListener('input', checkInputs);
    input.addEventListener('change', checkInputs);
});

async function createLead() {
    const payload = {
        customerName: document.getElementById('customerName').value.trim(),
        environment: document.getElementById('env').value.trim(),
        customerType: document.getElementById('customerType').value.trim(),
        userSelectedCityProperty: document.getElementById('city').value.trim(),
        mobileNoStarting2digitPrefix: document.getElementById('mobilePrefix').value.trim(),
        noOfLeads: document.getElementById('lead').value,
        showroomId: document.getElementById('showroom').value.trim(),
    };

    const responseContainer = document.getElementById('responseContainer');
    responseContainer.innerHTML = '';

    runBtn.textContent = 'Running...';
    runBtn.classList.remove('active');
    runBtn.classList.add('running');
    runBtn.disabled = true;

    try {
        // Get API base URL preferring same-origin; fall back to backend config
        let apiBaseUrl = window.location.origin;
        try {
            const configResponse = await fetch('/api/config');
            if (configResponse.ok) {
                const config = await configResponse.json();
                if (config.apiBaseUrl) {
                    apiBaseUrl = config.apiBaseUrl;
                }
            }
        } catch (_) {
            // ignore config errors; use same-origin
        }
        
        console.log('Using API base URL:', apiBaseUrl);
        
        // Ensure we're using the correct endpoint path
        const response = await fetch(`${apiBaseUrl}/LeadCreation`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = await response.json();

        console.log('✅ Lead Created Successfully:', data);

        // --- Extract fields safely ---
        const output = data.output || {};
        const customerIds = Array.isArray(output.customerId)  ? output.customerId : (output.customerId ? [output.customerId] : []);
        const projectUrls = Array.isArray(output.projectFullURL)
            ? output.projectFullURL
            : (output.projectFullURL ? [output.projectFullURL] : []);

     // --- Build Customer IDs HTML ---
     const customerIdsHtml = customerIds.length > 0
         ? customerIds.map(id => `<div class="response-item">• ${id}</div>`).join('')
         : '<div>N/A</div>';

     // --- Build Project URLs HTML ---
     const projectUrlsHtml = projectUrls.length > 0
         ? projectUrls.map(url => `<div class="response-item">• <a href="${url}" target="_blank">${url}</a></div>`).join('')
         : '<div>N/A</div>';


        // --- Display structured data in UI ---
        responseContainer.innerHTML = `
            <div><strong>Status:</strong> ${data.status}</div>
            <hr>
            <div><strong>Customer IDs:</strong></div>
            ${customerIdsHtml}
            <hr>
            <div><strong>Project URLs:</strong></div>
            ${projectUrlsHtml}
        `;

        // --- Button success state ---
        runBtn.textContent = 'Success';
        runBtn.classList.remove('running');
        runBtn.classList.add('success');
    } catch (err) {
        console.error('❌ API Call Failed:', err);
        responseContainer.innerHTML = `<div style="color:red;"><strong>Error:</strong> ${err.message}</div>`;
        runBtn.textContent = 'Failed';
        runBtn.classList.remove('running');
        runBtn.classList.add('error');
    } finally {
        setTimeout(() => {
            runBtn.textContent = 'Create Lead';
            runBtn.classList.remove('success', 'error', 'running');
            runBtn.classList.add('active');
            runBtn.disabled = false;
        }, 3000);
    }
}


runBtn.addEventListener('click', createLead);

clearBtn.addEventListener('click', () => {
    responseContainer.innerHTML = '';}); 
