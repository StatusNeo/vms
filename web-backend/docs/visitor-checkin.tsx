import React, { useState } from 'react';
import { ChevronDown, User, Mail, Phone, Building, Search } from 'lucide-react';

const VisitorCheckin = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    company: '',
    host: ''
  });
  
  const [otpSent, setOtpSent] = useState(false);
  const [otp, setOtp] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [hostDropdownOpen, setHostDropdownOpen] = useState(false);
  
  // Sample host employees data
  const hostEmployees = [
    { name: 'John Smith', email: 'john.smith@company.com' },
    { name: 'Sarah Johnson', email: 'sarah.johnson@company.com' },
    { name: 'Mike Davis', email: 'mike.davis@company.com' },
    { name: 'Emily Chen', email: 'emily.chen@company.com' },
    { name: 'David Wilson', email: 'david.wilson@company.com' },
    { name: 'Lisa Brown', email: 'lisa.brown@company.com' }
  ];
  
  const [filteredHosts, setFilteredHosts] = useState(hostEmployees);

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleHostSearch = (searchTerm) => {
    setFormData(prev => ({ ...prev, host: searchTerm }));
    const filtered = hostEmployees.filter(host => 
      host.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      host.email.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredHosts(filtered);
  };

  const handleHostSelect = (host) => {
    setFormData(prev => ({ ...prev, host: `${host.name} (${host.email})` }));
    setHostDropdownOpen(false);
    setFilteredHosts(hostEmployees);
  };

  const handleSendOTP = async () => {
    setIsSubmitting(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    setOtpSent(true);
    setIsSubmitting(false);
  };

  const handleVerifyOTP = async () => {
    setIsSubmitting(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500));
    alert('Registration successful! Welcome to our office.');
    setIsSubmitting(false);
  };

  const isFormValid = formData.name && formData.email && formData.phone && formData.company && formData.host;

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8 pt-8">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">Visitor Check-in</h1>
          <p className="text-gray-600">Please fill in your details to register your visit</p>
        </div>

        {/* Main Form Container */}
        <div className="bg-white rounded-2xl shadow-xl p-6 md:p-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            {/* Name Field */}
            <div className="space-y-2">
              <label className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <User className="w-4 h-4 mr-2 text-gray-500" />
                Full Name *
              </label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="Enter your full name"
              />
            </div>

            {/* Email Field */}
            <div className="space-y-2">
              <label className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <Mail className="w-4 h-4 mr-2 text-gray-500" />
                Email Address *
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => handleInputChange('email', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="Enter your email address"
              />
            </div>

            {/* Phone Field */}
            <div className="space-y-2">
              <label className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <Phone className="w-4 h-4 mr-2 text-gray-500" />
                Phone Number *
              </label>
              <input
                type="tel"
                value={formData.phone}
                onChange={(e) => handleInputChange('phone', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="Enter your phone number"
              />
            </div>

            {/* Company Field */}
            <div className="space-y-2">
              <label className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <Building className="w-4 h-4 mr-2 text-gray-500" />
                Company *
              </label>
              <input
                type="text"
                value={formData.company}
                onChange={(e) => handleInputChange('company', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="Enter your company name"
              />
            </div>

            {/* Host Employee Dropdown - Full Width */}
            <div className="md:col-span-2 space-y-2 relative">
              <label className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <Search className="w-4 h-4 mr-2 text-gray-500" />
                Who are you visiting? *
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={formData.host}
                  onChange={(e) => handleHostSearch(e.target.value)}
                  onFocus={() => setHostDropdownOpen(true)}
                  className="w-full px-4 py-3 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  placeholder="Search by name or email..."
                />
                <ChevronDown className="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                
                {hostDropdownOpen && (
                  <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-48 overflow-y-auto">
                    {filteredHosts.length > 0 ? (
                      filteredHosts.map((host, index) => (
                        <div
                          key={index}
                          onClick={() => handleHostSelect(host)}
                          className="px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
                        >
                          <div className="font-medium text-gray-900">{host.name}</div>
                          <div className="text-sm text-gray-500">{host.email}</div>
                        </div>
                      ))
                    ) : (
                      <div className="px-4 py-3 text-gray-500 text-center">No employees found</div>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* OTP Section */}
            {otpSent && (
              <div className="md:col-span-2 space-y-2">
                <label className="text-sm font-medium text-gray-700 mb-2 block">
                  Enter OTP sent to your phone *
                </label>
                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors text-center text-2xl tracking-widest"
                  placeholder="000000"
                  maxLength="6"
                />
              </div>
            )}
          </div>

          {/* Action Button */}
          <div className="mt-8 flex justify-center">
            {!otpSent ? (
              <button
                onClick={handleSendOTP}
                disabled={!isFormValid || isSubmitting}
                className="px-8 py-4 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors min-w-48 text-lg"
              >
                {isSubmitting ? 'Sending...' : 'Send OTP'}
              </button>
            ) : (
              <button
                onClick={handleVerifyOTP}
                disabled={!otp || otp.length !== 6 || isSubmitting}
                className="px-8 py-4 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors min-w-48 text-lg"
              >
                {isSubmitting ? 'Verifying...' : 'Verify & Check In'}
              </button>
            )}
          </div>

          {/* Footer */}
          <div className="mt-6 text-center text-sm text-gray-500">
            By checking in, you agree to our visitor policies and terms of service.
          </div>
        </div>
      </div>
    </div>
  );
};

export default VisitorCheckin;